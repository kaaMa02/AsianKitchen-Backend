package ch.asiankitchen.service;

import ch.asiankitchen.dto.HoursStatusDTO;
import ch.asiankitchen.model.RestaurantInfo;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HoursService {

    private final RestaurantInfoRepository infoRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.timezone:Europe/Zurich}")
    private String tzId;

    @Value("${app.delivery.close-buffer-minutes:45}")
    private int deliveryCloseBufferMinutes;

    /** openingHours JSON shape: { "1":[{"open":"11:00","close":"14:00"}, ...], "2":[...], ... } */
    public Map<Integer, List<Window>> loadWeekly() {
        RestaurantInfo info = infoRepo.findAll().stream().findFirst()
                .orElse(null);
        if (info == null || info.getOpeningHours() == null) return Map.of();

        try {
            return mapper.readValue(info.getOpeningHours(),
                    new TypeReference<Map<Integer, List<Window>>>() {});
        } catch (Exception e) {
            return Map.of(); // fallback: treat as closed if malformed
        }
    }

    public HoursStatusDTO status(boolean forDelivery) {
        ZoneId zone = ZoneId.of(tzId);
        ZonedDateTime now = ZonedDateTime.now(zone);

        Map<Integer, List<Window>> weekly = loadWeekly();
        int dow = now.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        List<Window> today = weekly.getOrDefault(dow, List.of());

        // No windows today
        if (today.isEmpty()) {
            return HoursStatusDTO.builder()
                    .openNow(false)
                    .reason(HoursStatusDTO.Reason.CLOSED_TODAY)
                    .message("We’re closed today.")
                    .build();
        }

        // Build concrete intervals today
        List<Interval> intervals = new ArrayList<>();
        for (Window w : today) {
            LocalTime o = parseTime(w.open);
            LocalTime c = parseTime(w.close);
            if (o == null || c == null) continue;
            ZonedDateTime start = now.withHour(o.getHour()).withMinute(o.getMinute())
                    .withSecond(0).withNano(0);
            ZonedDateTime end = now.withHour(c.getHour()).withMinute(c.getMinute())
                    .withSecond(0).withNano(0);
            intervals.add(new Interval(start, end));
        }
        intervals.sort(Comparator.comparing(i -> i.start));

        // Determine where 'now' falls
        Interval active = null;
        for (Interval it : intervals) {
            if (!now.isBefore(it.start) && now.isBefore(it.end)) {
                active = it;
                break;
            }
        }

        if (active != null) {
            // Open now. For DELIVERY, enforce close buffer
            if (forDelivery) {
                ZonedDateTime cutoff = active.end.minusMinutes(deliveryCloseBufferMinutes);
                if (!now.isBefore(cutoff)) {
                    return HoursStatusDTO.builder()
                            .openNow(false)
                            .reason(HoursStatusDTO.Reason.CUTOFF_DELIVERY)
                            .windowClosesAt(active.end)
                            .message("Ordering for delivery is closed as we’re near closing time.")
                            .build();
                }
            }
            return HoursStatusDTO.builder()
                    .openNow(true)
                    .reason(HoursStatusDTO.Reason.OPEN)
                    .windowOpensAt(active.start)
                    .windowClosesAt(active.end)
                    .message("We’re open.")
                    .build();
        }

        // Not inside a window: find next window today
        for (Interval it : intervals) {
            if (now.isBefore(it.start)) {
                return HoursStatusDTO.builder()
                        .openNow(false)
                        .reason(HoursStatusDTO.Reason.BEFORE_OPEN)
                        .windowOpensAt(it.start)
                        .message("We’ll open soon.")
                        .build();
            }
        }

        // After last window
        return HoursStatusDTO.builder()
                .openNow(false)
                .reason(HoursStatusDTO.Reason.AFTER_CLOSE)
                .windowClosesAt(intervals.get(intervals.size() - 1).end)
                .message("We’re closed for today.")
                .build();
    }

    /* ---------- helpers ---------- */

    private LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    public static class Window {
        public String open;
        public String close;
    }

    private record Interval(ZonedDateTime start, ZonedDateTime end) {}
}