package ch.asiankitchen.service;

import ch.asiankitchen.dto.HoursStatusDTO;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
@RequiredArgsConstructor
public class HoursService {

    private final RestaurantInfoRepository infoRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.timezone:Europe/Zurich}")
    private String tzId;

    /** Minutes before close where DELIVERY should be disallowed. */
    @Value("${app.delivery.close-buffer-minutes:45}")
    private int deliveryCloseBufferMinutes;

    /** Minimal prep lead for ASAP orders (server-authoritative). Backward compatible key. */
    @Value("${app.order.min-prep-minutes:${app.order.min-lead-minutes:45}}")
    private int minPrepMinutes;

    /** Minutes after opening required for scheduled orders created while closed. */
    @Value("${app.order.schedule-minutes-after-open-when-closed:60}")
    private int minutesAfterOpenWhenClosed;

    /** openingHours JSON shape (canonical): { "1":[{"open":"11:00","close":"14:00"}, ...], ... } */
    public Map<Integer, List<Window>> loadWeekly() {
        var info = infoRepo.findAll().stream().findFirst().orElse(null);
        if (info == null) return Map.of();
        String s = info.getOpeningHours();
        if (s == null || s.isBlank()) return Map.of();

        // Primary: canonical numeric-day format
        try {
            return mapper.readValue(s, new TypeReference<Map<Integer, List<Window>>>() {});
        } catch (Exception ignored) { }

        // Fallback: accept day-name keys (Mon..Sun) just in case older data slips in
        try {
            Map<String, List<Window>> byName =
                    mapper.readValue(s, new TypeReference<Map<String, List<Window>>>() {});
            Map<Integer, List<Window>> norm = new HashMap<>();
            byName.forEach((k, v) -> {
                int d = switch (k.trim().toLowerCase(Locale.ROOT)) {
                    case "1","01","mon","monday" -> 1;
                    case "2","02","tue","tues","tuesday" -> 2;
                    case "3","03","wed","wednesday" -> 3;
                    case "4","04","thu","thur","thurs","thursday" -> 4;
                    case "5","05","fri","friday" -> 5;
                    case "6","06","sat","saturday" -> 6;
                    case "7","07","sun","sunday" -> 7;
                    default -> 0;
                };
                if (d != 0) norm.put(d, v);
            });
            return norm;
        } catch (Exception ignored) { }

        // Last resort: treat as closed to be safe
        return Map.of();
    }

    /** Convenience: status for NOW. */
    public HoursStatusDTO statusNow(boolean forDelivery) {
        return statusAt(Instant.now(), forDelivery);
    }

    /** Core: “what’s my status at the given instant?” */
    public HoursStatusDTO statusAt(Instant atInstant, boolean forDelivery) {
        ZoneId zone = ZoneId.of(tzId);
        ZonedDateTime at = atInstant.atZone(zone);

        Map<Integer, List<Window>> weekly = loadWeekly();
        var intervalsToday = buildIntervalsForDate(at.toLocalDate(), weekly, zone);
        var intervalsYesterday = buildIntervalsForDate(at.toLocalDate().minusDays(1), weekly, zone);

        List<Interval> relevant = new ArrayList<>();
        for (Interval it : intervalsYesterday) {
            if (it.end.isAfter(at.with(LocalTime.MIDNIGHT))) relevant.add(it);
        }
        relevant.addAll(intervalsToday);
        relevant.sort(Comparator.comparing(i -> i.start));

        Interval active = null;
        for (Interval it : relevant) {
            if (!at.isBefore(it.start) && at.isBefore(it.end)) { active = it; break; }
        }

        if (active != null) {
            if (forDelivery) {
                ZonedDateTime cutoff = active.end.minusMinutes(deliveryCloseBufferMinutes);
                if (!at.isBefore(cutoff)) {
                    return HoursStatusDTO.builder()
                            .openNow(false)
                            .reason(HoursStatusDTO.Reason.CUTOFF_DELIVERY)
                            .windowOpensAt(active.start.toInstant())
                            .windowClosesAt(active.end.toInstant())
                            .message("Delivery ordering is closed near closing time.")
                            .build();
                }
            }
            return HoursStatusDTO.builder()
                    .openNow(true)
                    .reason(HoursStatusDTO.Reason.OPEN)
                    .windowOpensAt(active.start.toInstant())
                    .windowClosesAt(active.end.toInstant())
                    .message("We’re open.")
                    .build();
        }

        Optional<Interval> next = nextIntervalStartingAfter(at, weekly, zone, 14);
        String msg;
        HoursStatusDTO.Reason reason;

        boolean hadAnyToday = !intervalsToday.isEmpty();
        boolean beforeFirstToday = hadAnyToday && at.isBefore(intervalsToday.get(0).start);
        boolean afterLastToday = hadAnyToday && at.isAfter(intervalsToday.get(intervalsToday.size()-1).end);
        boolean betweenTodayWindows = hadAnyToday && !beforeFirstToday && !afterLastToday;

        if (!hadAnyToday) { reason = HoursStatusDTO.Reason.CLOSED_TODAY; msg = "We’re closed today."; }
        else if (beforeFirstToday) { reason = HoursStatusDTO.Reason.BEFORE_OPEN; msg = "We’ll open soon."; }
        else if (betweenTodayWindows) { reason = HoursStatusDTO.Reason.BETWEEN_WINDOWS; msg = "We’ll reopen later today."; }
        else { reason = HoursStatusDTO.Reason.AFTER_CLOSE; msg = "We’re closed for today."; }

        return HoursStatusDTO.builder()
                .openNow(false)
                .reason(reason)
                .windowOpensAt(next.map(n -> n.start.toInstant()).orElse(null))
                .windowClosesAt(next.map(n -> n.end.toInstant()).orElse(null))
                .message(msg)
                .build();
    }

    /**
     * Guard for placing an order:
     * - ASAP: NOW must be open; target = now + minPrepMinutes must also be inside an open window (and delivery cutoff respected).
     * - Scheduled: allowed while closed only if requestedAt ≥ (nextOpen + minutesAfterOpenWhenClosed);
     *   and target window must be open (with cutoff).
     */
    public void assertOrderAllowed(boolean forDelivery, boolean asap, Instant scheduledAt) {
        Instant now = Instant.now();

        if (!asap && scheduledAt == null) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "REQUESTED_AT_REQUIRED");
        }

        if (asap) {
            HoursStatusDTO nowStatus = statusAt(now, forDelivery);
            if (!nowStatus.isOpenNow()) {
                throw new ResponseStatusException(CONFLICT, "ASAP_NOT_ALLOWED_WHEN_CLOSED");
            }
        } else {
            HoursStatusDTO nowStatus = statusAt(now, forDelivery);
            if (!nowStatus.isOpenNow()) {
                Optional<ZonedDateTime> nextOpen = nextOpeningAfter(now);
                if (nextOpen.isEmpty()) {
                    throw new ResponseStatusException(CONFLICT, "RESTAURANT_CLOSED_NO_NEXT_OPEN");
                }
                Instant minAllowed = nextOpen.get().plusMinutes(minutesAfterOpenWhenClosed).toInstant();
                if (scheduledAt.isBefore(minAllowed)) {
                    throw new ResponseStatusException(CONFLICT, "SCHEDULE_TOO_SOON_AFTER_OPEN");
                }
            }
        }

        // Target time check (cutoff included)
        Instant target = asap ? now.plus(Duration.ofMinutes(minPrepMinutes)) : scheduledAt;
        HoursStatusDTO targetStatus = statusAt(target, forDelivery);
        if (!targetStatus.isOpenNow()) {
            throw new ResponseStatusException(CONFLICT, "RESTAURANT_CLOSED_AT_TARGET");
        }
    }

    /** Next opening start after the given instant (in restaurant zone). */
    public Optional<ZonedDateTime> nextOpeningAfter(Instant from) {
        ZoneId zone = ZoneId.of(tzId);
        ZonedDateTime probe = from.atZone(zone);
        return nextIntervalStartingAfter(probe, loadWeekly(), zone, 21).map(i -> i.start);
    }

    /* --------------------------------- helpers --------------------------------- */

    private List<Interval> buildIntervalsForDate(LocalDate date, Map<Integer, List<Window>> weekly, ZoneId zone) {
        int dow = date.getDayOfWeek().getValue(); // 1..7
        List<Window> windows = weekly.getOrDefault(dow, List.of());
        List<Interval> out = new ArrayList<>();
        for (Window w : windows) {
            LocalTime open = parseTime(w.open);
            LocalTime close = parseTime(w.close);
            if (open == null || close == null) continue;

            ZonedDateTime start = date.atTime(open).atZone(zone);
            ZonedDateTime end = date.atTime(close).atZone(zone);
            if (close.isBefore(open)) end = end.plusDays(1); // overnight
            out.add(new Interval(start, end));
        }
        out.sort(Comparator.comparing(i -> i.start));
        return out;
    }

    private Optional<Interval> nextIntervalStartingAfter(ZonedDateTime probe,
                                                         Map<Integer, List<Window>> weekly,
                                                         ZoneId zone,
                                                         int maxDays) {
        ZonedDateTime cursor = probe;
        for (int d = 0; d <= maxDays; d++) {
            LocalDate date = cursor.toLocalDate();
            for (Interval it : buildIntervalsForDate(date, weekly, zone)) {
                if (it.start.isAfter(probe)) return Optional.of(it);
            }
            cursor = cursor.plusDays(1).with(LocalTime.MIDNIGHT);
        }
        return Optional.empty();
    }

    private LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    // Public so RestaurantInfoService can reuse for normalization
    public static class Window { public String open; public String close; }
    private record Interval(ZonedDateTime start, ZonedDateTime end) {}
}
