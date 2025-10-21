// backend/src/main/java/ch/asiankitchen/scheduler/OrderEscalationScheduler.java
package ch.asiankitchen.scheduler;

import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import ch.asiankitchen.service.EmailService;
import ch.asiankitchen.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Component
@RequiredArgsConstructor
public class OrderEscalationScheduler {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final ReservationRepository reservationRepo;
    private final EmailService mailService;
    private final WebPushService webPush;

    @Value("${app.mail.to.escalation:}")
    private String escalationEmail;

    @Value("${app.timezone:Europe/Zurich}")
    private String appTz;

    @Value("${app.order.escalate-minutes:5}")
    private int escalateMinutes;

    @Value("${app.order.autocancel-minutes:15}")
    private int autocancelMinutes;

    private static final java.time.format.DateTimeFormatter CH_FMT = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private String fmtLocal(LocalDateTime utcTs) {
        if (utcTs == null) return "—";
        return utcTs.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of(appTz)).format(CH_FMT);
    }

    // tick every second
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void tick() {
        var now = LocalDateTime.now(ZoneOffset.UTC);

        // ───────────── Customer (menu) orders ─────────────
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            boolean dirty = false;

            if (o.getAutoCancelAt() == null) {
                o.setAutoCancelAt(o.getCreatedAt().plusMinutes(autocancelMinutes));
                dirty = true;
            }

            if (o.getSeenAt() == null &&
                    o.getEscalatedAt() == null &&
                    !now.isBefore(o.getCreatedAt().plusMinutes(escalateMinutes))) {
                o.setEscalatedAt(now);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Order waiting\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try { mailService.sendSimple(escalationEmail, "Order waiting action", "Order " + o.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            // cancel only if still unseen at autoCancelAt and not already paid
            if (o.getSeenAt() == null &&
                    o.getAutoCancelAt() != null &&
                    !now.isBefore(o.getAutoCancelAt()) &&
                    o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}

                // Owner notification (no action → auto-cancel), include details
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    String customer = (o.getCustomerInfo() == null) ? "(no customer info)" :
                            """
                            %s %s
                            %s
                            %s %s
                            %s %s
                            """.formatted(
                                    nullSafe(o.getCustomerInfo().getFirstName()),
                                    nullSafe(o.getCustomerInfo().getLastName()),
                                    nullSafe(o.getCustomerInfo().getEmail()),
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getStreet()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getStreetNo()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getPlz()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getCity()) : ""
                            ).trim();

                    String body = """
                            AUTO-CANCELLED — Menu order (no admin action in time)

                            Order ID: %s
                            Created:  %s
                            Requested: %s
                            Total:    CHF %s

                            Customer:
                            %s
                            """.formatted(
                            o.getId(),
                            fmtLocal(o.getCreatedAt()),
                            o.isAsap() ? "ASAP" : fmtLocal(o.getRequestedAt()),
                            String.valueOf(o.getTotalPrice()),
                            customer
                    );
                    try { mailService.sendSimple(escalationEmail, "Auto-cancelled — Order " + o.getId(), body, null); } catch (Exception ignored) {}
                }
            }

            if (dirty) customerOrderRepo.save(o);
        });

        // ───────────── Buffet orders ─────────────
        buffetOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            boolean dirty = false;

            if (o.getAutoCancelAt() == null) {
                o.setAutoCancelAt(o.getCreatedAt().plusMinutes(autocancelMinutes));
                dirty = true;
            }

            if (o.getSeenAt() == null &&
                    o.getEscalatedAt() == null &&
                    !now.isBefore(o.getCreatedAt().plusMinutes(escalateMinutes))) {
                o.setEscalatedAt(now);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Order waiting\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try { mailService.sendSimple(escalationEmail, "Order waiting action", "Buffet order " + o.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            if (o.getSeenAt() == null &&
                    o.getAutoCancelAt() != null &&
                    !now.isBefore(o.getAutoCancelAt()) &&
                    o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}

                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    String customer = (o.getCustomerInfo() == null) ? "(no customer info)" :
                            """
                            %s %s
                            %s
                            %s %s
                            %s %s
                            """.formatted(
                                    nullSafe(o.getCustomerInfo().getFirstName()),
                                    nullSafe(o.getCustomerInfo().getLastName()),
                                    nullSafe(o.getCustomerInfo().getEmail()),
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getStreet()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getStreetNo()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getPlz()) : "",
                                    o.getCustomerInfo().getAddress() != null ? nullSafe(o.getCustomerInfo().getAddress().getCity()) : ""
                            ).trim();

                    String body = """
                            AUTO-CANCELLED — Buffet order (no admin action in time)

                            Order ID: %s
                            Created:  %s
                            Requested: %s
                            Total:    CHF %s

                            Customer:
                            %s
                            """.formatted(
                            o.getId(),
                            fmtLocal(o.getCreatedAt()),
                            o.isAsap() ? "ASAP" : fmtLocal(o.getRequestedAt()),
                            String.valueOf(o.getTotalPrice()),
                            customer
                    );
                    try { mailService.sendSimple(escalationEmail, "Auto-cancelled — Buffet " + o.getId(), body, null); } catch (Exception ignored) {}
                }
            }

            if (dirty) buffetOrderRepo.save(o);
        });

        // ───────────── Reservations ─────────────
        reservationRepo.findAllByStatus(ReservationStatus.REQUESTED).forEach(r -> {
            boolean dirty = false;

            if (r.getAutoCancelAt() == null) {
                r.setAutoCancelAt(r.getCreatedAt().plusMinutes(autocancelMinutes));
                dirty = true;
            }

            if (r.getSeenAt() == null &&
                    r.getEscalatedAt() == null &&
                    !now.isBefore(r.getCreatedAt().plusMinutes(escalateMinutes))) {
                r.setEscalatedAt(now);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Reservation waiting\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try { mailService.sendSimple(escalationEmail, "Reservation waiting action", "Reservation " + r.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            if (r.getSeenAt() == null &&
                    r.getAutoCancelAt() != null &&
                    !now.isBefore(r.getAutoCancelAt()) &&
                    r.getStatus() != ReservationStatus.CONFIRMED) {
                r.setStatus(ReservationStatus.CANCELLED);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Reservation auto-cancelled\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}

                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    String customer = (r.getCustomerInfo() == null) ? "(no customer info)" :
                            """
                            %s %s
                            %s
                            %s %s
                            %s %s
                            """.formatted(
                                    nullSafe(r.getCustomerInfo().getFirstName()),
                                    nullSafe(r.getCustomerInfo().getLastName()),
                                    nullSafe(r.getCustomerInfo().getEmail()),
                                    r.getCustomerInfo().getAddress() != null ? nullSafe(r.getCustomerInfo().getAddress().getStreet()) : "",
                                    r.getCustomerInfo().getAddress() != null ? nullSafe(r.getCustomerInfo().getAddress().getStreetNo()) : "",
                                    r.getCustomerInfo().getAddress() != null ? nullSafe(r.getCustomerInfo().getAddress().getPlz()) : "",
                                    r.getCustomerInfo().getAddress() != null ? nullSafe(r.getCustomerInfo().getAddress().getCity()) : ""
                            ).trim();

                    String body = """
                            AUTO-CANCELLED — Reservation (no admin action in time)

                            Reservation ID: %s
                            Created:        %s
                            Reserved time:  %s

                            Customer:
                            %s
                            """.formatted(
                            r.getId(),
                            fmtLocal(r.getCreatedAt()),
                            fmtLocal(r.getReservationDateTime()),
                            customer
                    );
                    try { mailService.sendSimple(escalationEmail, "Auto-cancelled — Reservation " + r.getId(), body, null); } catch (Exception ignored) {}
                }
            }

            if (dirty) reservationRepo.save(r);
        });
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
