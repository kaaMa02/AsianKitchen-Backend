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

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderEscalationScheduler {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final ReservationRepository reservationRepo;
    private final EmailService mailService;
    private final WebPushService webPush;

    @Value("${app.mail.to.escalation}")
    private String escalationEmail;

    @Value("${app.order.escalate-minutes:5}")
    private int escalateMinutes;

    @Value("${app.order.autocancel-minutes:15}")
    private int autocancelMinutes;

    // tick every second
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void tick() {
        var now = LocalDateTime.now();

        // ───────────── Customer (menu) orders ─────────────
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            boolean dirty = false;

            // backfill autoCancelAt for legacy rows
            if (o.getAutoCancelAt() == null) {
                o.setAutoCancelAt(o.getCreatedAt().plusMinutes(autocancelMinutes));
                dirty = true;
            }

            // escalate once if unseen after escalateMinutes
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
                    try { mailService.sendSimple(escalationEmail, "Order waiting action", "Order " + o.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            if (o.getSeenAt() == null &&
                    o.getAutoCancelAt() != null &&
                    !now.isBefore(o.getAutoCancelAt()) &&
                    o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}
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

            // cancel only if still unseen at autoCancelAt and not confirmed
            if (r.getSeenAt() == null &&
                    r.getAutoCancelAt() != null &&
                    !now.isBefore(r.getAutoCancelAt()) &&
                    r.getStatus() != ReservationStatus.CONFIRMED) {
                r.setStatus(ReservationStatus.CANCELLED);
                dirty = true;
                try { webPush.broadcast("admin", "{\"title\":\"Reservation auto-cancelled\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}
            }

            if (dirty) reservationRepo.save(r);
        });
    }
}
