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
import java.util.Optional;

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

    @Value("${app.order.autocancel-minutes:10}") // 10 minutes per your change
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
                if (hasEscalation()) {
                    try { mailService.sendSimple(escalationEmail, "Order waiting action", "Order " + o.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            // NEW rule: auto-cancel regardless of payment status if unseen at deadline
            if (o.getSeenAt() == null &&
                    o.getAutoCancelAt() != null &&
                    !now.isBefore(o.getAutoCancelAt())) {

                o.setStatus(OrderStatus.CANCELLED);
                dirty = true;

                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}

                // Email customer (different line for paid card vs not)
                autoCancelEmailToCustomer(o);

                // Email owner (different wording if refund required)
                autoCancelEmailToOwnerForCustomerOrder(o);
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
                if (hasEscalation()) {
                    try { mailService.sendSimple(escalationEmail, "Order waiting action", "Buffet order " + o.getId() + " needs attention.", null); } catch (Exception ignored) {}
                }
            }

            // NEW: auto-cancel even if paid (unseen)
            if (o.getSeenAt() == null &&
                    o.getAutoCancelAt() != null &&
                    !now.isBefore(o.getAutoCancelAt())) {

                o.setStatus(OrderStatus.CANCELLED);
                dirty = true;

                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}

                autoCancelEmailToCustomer(o);
                autoCancelEmailToOwnerForBuffet(o);
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
                if (hasEscalation()) {
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

                autoCancelEmailToCustomer(r);
                autoCancelEmailToOwnerForReservation(r);
            }

            if (dirty) reservationRepo.save(r);
        });
    }

    /* ───────────────────────────── helpers ───────────────────────────── */

    private boolean hasEscalation() {
        return escalationEmail != null && !escalationEmail.isBlank();
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private String customerBlock(CustomerInfo ci) {
        if (ci == null) return "(no customer info)";
        var addr = ci.getAddress();
        String line1 = (addr != null)
                ? (nullSafe(addr.getStreet()) + " " + nullSafe(addr.getStreetNo())).trim()
                : "";
        String line2 = (addr != null)
                ? (nullSafe(addr.getPlz()) + " " + nullSafe(addr.getCity())).trim()
                : "";

        return ("""
                %s %s
                %s
                %s
                %s
                """.formatted(
                nullSafe(ci.getFirstName()),
                nullSafe(ci.getLastName()),
                nullSafe(ci.getEmail()),
                line1,
                line2
        )).trim();
    }

    /* ----- Auto-cancel: customer emails ----- */

    private void autoCancelEmailToCustomer(CustomerOrder o) {
        String to = Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getEmail).orElse(null);
        if (to == null || to.isBlank()) return;

        boolean paidCard = o.getPaymentStatus() == PaymentStatus.SUCCEEDED;

        String subject = "Order auto-cancelled — Asian Kitchen";
        String body = """
                Hi %s,

                We’re sorry — your order was automatically cancelled because we couldn’t confirm it in time.
                %s

                Order ID: %s
                Placed:   %s
                Deliver:  %s
                Total:    CHF %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getFirstName).orElse(""),
                paidCard
                        ? "\nWe’ll refund your card payment.\n"
                        : "\nNo online charge was made.\n",
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                String.valueOf(o.getTotalPrice())
        );

        try { mailService.sendSimple(to, subject, body, null); } catch (Exception ignored) {}
    }

    private void autoCancelEmailToCustomer(BuffetOrder o) {
        String to = Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getEmail).orElse(null);
        if (to == null || to.isBlank()) return;

        boolean paidCard = o.getPaymentStatus() == PaymentStatus.SUCCEEDED;

        String subject = "Buffet order auto-cancelled — Asian Kitchen";
        String body = """
                Hi %s,

                We’re sorry — your buffet order was automatically cancelled because we couldn’t confirm it in time.
                %s

                Order ID: %s
                Placed:   %s
                Deliver:  %s
                Total:    CHF %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getFirstName).orElse(""),
                paidCard
                        ? "\nWe’ll refund your card payment.\n"
                        : "\nNo online charge was made.\n",
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                String.valueOf(o.getTotalPrice())
        );

        try { mailService.sendSimple(to, subject, body, null); } catch (Exception ignored) {}
    }

    private void autoCancelEmailToCustomer(Reservation r) {
        String to = Optional.ofNullable(r.getCustomerInfo()).map(CustomerInfo::getEmail).orElse(null);
        if (to == null || to.isBlank()) return;

        String subject = "Reservation auto-cancelled — Asian Kitchen";
        String body = """
                Hi %s,

                We’re sorry — your reservation request was automatically cancelled because we couldn’t confirm it in time.

                Reservation ID: %s
                Requested time: %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(r.getCustomerInfo()).map(CustomerInfo::getFirstName).orElse(""),
                r.getId(),
                fmtLocal(r.getReservationDateTime())
        );

        try { mailService.sendSimple(to, subject, body, null); } catch (Exception ignored) {}
    }

    /* ----- Auto-cancel: owner emails ----- */

    private void autoCancelEmailToOwnerForCustomerOrder(CustomerOrder o) {
        if (!hasEscalation()) return;

        boolean paidCard = o.getPaymentStatus() == PaymentStatus.SUCCEEDED;
        String customer = (o.getCustomerInfo() == null) ? "(no customer info)" : customerBlock(o.getCustomerInfo());

        String title = paidCard
                ? ("Auto-cancelled — Order " + o.getId() + " (refund needed)")
                : ("Auto-cancelled — Order " + o.getId());

        String body = """
                The team in Asian Kitchen didn't see the order, so the order was auto-cancelled.

                Order ID: %s
                Created:  %s
                Requested: %s
                Total:    CHF %s

                %s

                Customer:
                %s
                """.formatted(
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                String.valueOf(o.getTotalPrice()),
                paidCard ? "Refund needed, paid by card." : "No online charge was made.",
                customer
        );

        try { mailService.sendSimple(escalationEmail, title, body, null); } catch (Exception ignored) {}
    }

    private void autoCancelEmailToOwnerForBuffet(BuffetOrder o) {
        if (!hasEscalation()) return;

        boolean paidCard = o.getPaymentStatus() == PaymentStatus.SUCCEEDED;
        String customer = (o.getCustomerInfo() == null) ? "(no customer info)" : customerBlock(o.getCustomerInfo());

        String title = paidCard
                ? ("Auto-cancelled — Buffet " + o.getId() + " (refund needed)")
                : ("Auto-cancelled — Buffet " + o.getId());

        String body = """
                The team in Asian Kitchen didn't see the order, so the order was auto-cancelled.

                Order ID: %s
                Created:  %s
                Requested: %s
                Total:    CHF %s

                %s

                Customer:
                %s
                """.formatted(
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                String.valueOf(o.getTotalPrice()),
                paidCard ? "Refund needed, paid by card." : "No online charge was made.",
                customer
        );

        try { mailService.sendSimple(escalationEmail, title, body, null); } catch (Exception ignored) {}
    }

    private void autoCancelEmailToOwnerForReservation(Reservation r) {
        if (!hasEscalation()) return;

        String customer = (r.getCustomerInfo() == null) ? "(no customer info)" : customerBlock(r.getCustomerInfo());

        String title = "Auto-cancelled — Reservation " + r.getId();
        String body = """
                The team in Asian Kitchen didn't see the reservation request, so the reservation was auto-cancelled. Please call the customer.

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

        try { mailService.sendSimple(escalationEmail, title, body, null); } catch (Exception ignored) {}
    }
}
