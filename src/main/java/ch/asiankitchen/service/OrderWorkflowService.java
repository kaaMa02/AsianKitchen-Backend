// backend/src/main/java/ch/asiankitchen/service/OrderWorkflowService.java
package ch.asiankitchen.service;

import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderWorkflowService {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final ReservationRepository reservationRepo;
    private final ReservationEmailService reservationEmailService;
    private final EmailService mailService;
    private final WebPushService webPushService;

    @Value("${app.order.min-prep-minutes:45}")
    private int defaultMinPrep;

    @Value("${app.order.alert-seconds:60}")
    private int alertSeconds;

    @Value("${app.order.escalate-minutes:5}")
    private int escalateMinutes;

    @Value("${app.order.autocancel-minutes:15}")
    private int autocancelMinutes;

    @Value("${app.timezone:Europe/Zurich}")
    private String appTz;

    @Value("${app.mail.to.owner:}")
    private String otherOwnerEmail;

    private LocalDateTime toUtc(LocalDateTime local) {
        if (local == null) return null;
        return local.atZone(ZoneId.of(appTz))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private static final DateTimeFormatter CH_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private String fmtLocal(LocalDateTime utcTs) {
        if (utcTs == null) return "—";
        return utcTs.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of(appTz)).format(CH_FMT);
    }

    // ───── initial timing on creation (and 60s alert window placeholder) ─────
    public void applyInitialTiming(CustomerOrder o) {
        if (o.getMinPrepMinutes() == null || o.getMinPrepMinutes() <= 0) o.setMinPrepMinutes(defaultMinPrep);
        if (o.getAdminExtraMinutes() == null) o.setAdminExtraMinutes(0);

        // normalize requestedAt from local CH to UTC
        if (!o.isAsap() && o.getRequestedAt() != null) {
            o.setRequestedAt(toUtc(o.getRequestedAt()));
        }

        var basis = (o.getCreatedAt() != null) ? o.getCreatedAt() : LocalDateTime.now(ZoneOffset.UTC);
        o.setCommittedReadyAt(o.isAsap()
                ? basis.plusMinutes(o.getMinPrepMinutes() + o.getAdminExtraMinutes())
                : o.getRequestedAt());

        o.setAutoCancelAt(basis.plusMinutes(autocancelMinutes));
    }

    public void applyInitialTiming(BuffetOrder o) {
        if (o.getMinPrepMinutes() == null || o.getMinPrepMinutes() <= 0) o.setMinPrepMinutes(defaultMinPrep);
        if (o.getAdminExtraMinutes() == null) o.setAdminExtraMinutes(0);

        if (!o.isAsap() && o.getRequestedAt() != null) {
            o.setRequestedAt(toUtc(o.getRequestedAt()));
        }

        var basis = (o.getCreatedAt() != null) ? o.getCreatedAt() : LocalDateTime.now(ZoneOffset.UTC);
        o.setCommittedReadyAt(o.isAsap()
                ? basis.plusMinutes(o.getMinPrepMinutes() + o.getAdminExtraMinutes())
                : o.getRequestedAt());

        o.setAutoCancelAt(basis.plusMinutes(autocancelMinutes));
    }

    // ───── admin interactions ─────
    @Transactional
    public void markSeen(String kind, UUID id) {
        switch (kind) {
            case "menu" -> customerOrderRepo.findById(id).ifPresent(o -> {
                o.setSeenAt(LocalDateTime.now(ZoneOffset.UTC));
                customerOrderRepo.save(o);
            });
            case "buffet" -> buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setSeenAt(LocalDateTime.now(ZoneOffset.UTC));
                buffetOrderRepo.save(o);
            });
            case "reservation" -> reservationRepo.findById(id).ifPresent(r -> {
                r.setSeenAt(LocalDateTime.now(ZoneOffset.UTC));
                reservationRepo.save(r);
            });
            default -> {}
        }
    }

    @Transactional
    public void patchExtraMinutes(String kind, UUID id, int extra) {
        final int add = Math.max(0, extra);
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                    customerOrderRepo.save(o);
                }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                    buffetOrderRepo.save(o);
                }
            });
        } else if ("reservation".equals(kind)) {
            reservationRepo.findById(id).ifPresent(r -> {
                r.setStatus(ReservationStatus.CONFIRMED);
                reservationRepo.save(r);
                try { webPushService.broadcast("admin", "{\"title\":\"Reservation confirmed\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}
                try { reservationEmailService.sendConfirmationToCustomer(r); } catch (Exception ignored) {}
            });
        }
    }

    // Back-compat: older callers
    @Transactional
    public void confirmOrder(String kind, UUID id, boolean print) {
        confirmOrder(kind, id, null, print);
    }

    // Preferred: allow inline extraMinutes (from UI bubbles) and optional print flag
    @Transactional
    public void confirmOrder(String kind, UUID id, @Nullable Integer extraMinutes, boolean print) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                boolean adjusted = false;
                if (extraMinutes != null && o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    int add = Math.max(0, extraMinutes);
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                    adjusted = true;
                }
                o.setStatus(OrderStatus.CONFIRMED);
                customerOrderRepo.save(o);

                try { webPushService.broadcast("admin", "{\"title\":\"Order confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}

                // Email customer: "approx. XX minutes" + tracking link
                try { sendEtaEmailAfterConfirm(o); } catch (Exception ignored){}

                if (print) { /* optional: integrate printer hook if you keep server-side printing */ }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                if (extraMinutes != null && o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    int add = Math.max(0, extraMinutes);
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                }
                o.setStatus(OrderStatus.CONFIRMED);
                buffetOrderRepo.save(o);
                try { webPushService.broadcast("admin", "{\"title\":\"Buffet confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}

                try { sendEtaEmailAfterConfirm(o); } catch (Exception ignored){}
            });
        } else if ("reservation".equals(kind)) {
            // reservations: confirmation happens via patchExtraMinutes() path above
            reservationRepo.findById(id).ifPresent(r -> {
                r.setStatus(ReservationStatus.CONFIRMED);
                reservationRepo.save(r);
                try { webPushService.broadcast("admin", "{\"title\":\"Reservation confirmed\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}
                try { reservationEmailService.sendConfirmationToCustomer(r); } catch (Exception ignored) {}
            });
        }
    }

    // Overload without print (most callers)
    @Transactional
    public void confirmOrder(String kind, UUID id, @Nullable Integer extraMinutes) {
        confirmOrder(kind, id, extraMinutes, false);
    }

    @Transactional
    public void cancelOrder(String kind, UUID id, String reason, boolean refundIfPaid) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                customerOrderRepo.save(o);

                // Notify owner if paid via Stripe (SUCCEEDED) → manual refund needed
                if (o.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
                    notifyOwnerRefundNeeded("Menu", o.getId(), o.getCustomerInfo(), o, reason);
                }

                try { /* optionally email customer */ } catch (Exception ignored){}
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                buffetOrderRepo.save(o);

                if (o.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
                    notifyOwnerRefundNeeded("Buffet", o.getId(), o.getCustomerInfo(), o, reason);
                }

                try { /* optionally email customer */ } catch (Exception ignored){}
            });
        } else if ("reservation".equals(kind)) {
            reservationRepo.findById(id).ifPresent(r -> {
                r.setStatus(ReservationStatus.CANCELLED);
                reservationRepo.save(r);
                try { reservationEmailService.sendRejectionToCustomer(r); } catch (Exception ignored){}
            });
        }
    }

    // ───── helpers ─────

    private long minutesUntil(LocalDateTime utcTarget) {
        if (utcTarget == null) return 0;
        var nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        return Math.max(0, Duration.between(nowUtc, utcTarget).toMinutes());
    }

    private void sendEtaEmailAfterConfirm(CustomerOrder o) {
        String to = Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getEmail).orElse(null);
        if (to == null || to.isBlank()) return;

        long etaMin;
        if (o.isAsap()) {
            int base = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
            int extra = Optional.ofNullable(o.getAdminExtraMinutes()).orElse(0);
            etaMin = base + extra;
        } else {
            etaMin = minutesUntil(o.getCommittedReadyAt());
        }

        String trackUrl = "https://asian-kitchen.online/track?orderId=%s&email=%s"
                .formatted(o.getId(), o.getCustomerInfo().getEmail());

        String subject = "Order confirmed — Asian Kitchen";
        String body = """
                Hi %s,

                The order will be delivered approx. %d minutes.

                Track your order:
                %s

                Order ID: %s
                Placed:   %s
                Deliver:  %s
                Total:    CHF %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(o.getCustomerInfo().getFirstName()).orElse(""),
                etaMin,
                trackUrl,
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                o.getTotalPrice()
        );

        mailService.sendSimple(to, subject, body, null);
    }

    private void sendEtaEmailAfterConfirm(BuffetOrder o) {
        String to = Optional.ofNullable(o.getCustomerInfo()).map(CustomerInfo::getEmail).orElse(null);
        if (to == null || to.isBlank()) return;

        long etaMin;
        if (o.isAsap()) {
            int base = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
            int extra = Optional.ofNullable(o.getAdminExtraMinutes()).orElse(0);
            etaMin = base + extra;
        } else {
            etaMin = minutesUntil(o.getCommittedReadyAt());
        }

        String trackUrl = "https://asian-kitchen.online/track-buffet?orderId=%s&email=%s"
                .formatted(o.getId(), o.getCustomerInfo().getEmail());

        String subject = "Buffet order confirmed — Asian Kitchen";
        String body = """
                Hi %s,

                The order will be delivered approx. %d minutes.

                Track your order:
                %s

                Order ID: %s
                Placed:   %s
                Deliver:  %s
                Total:    CHF %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(o.getCustomerInfo().getFirstName()).orElse(""),
                etaMin,
                trackUrl,
                o.getId(),
                fmtLocal(o.getCreatedAt()),
                o.isAsap() ? "ASAP" : fmtLocal(o.getCommittedReadyAt()),
                o.getTotalPrice()
        );

        mailService.sendSimple(to, subject, body, null);
    }

    private void notifyOwnerRefundNeeded(String kind, UUID id, CustomerInfo ci, Object orderLike, String reason) {
        if (otherOwnerEmail == null || otherOwnerEmail.isBlank()) return;

        String customerBlock = (ci == null) ? "(no customer info)" :
                """
                %s %s
                %s
                %s %s %s %s
                """.formatted(
                        Optional.ofNullable(ci.getFirstName()).orElse(""),
                        Optional.ofNullable(ci.getLastName()).orElse(""),
                        Optional.ofNullable(ci.getPhone()).orElse(""),
                        ci.getAddress() != null ? Optional.ofNullable(ci.getAddress().getStreet()).orElse("") : "",
                        ci.getAddress() != null ? Optional.ofNullable(ci.getAddress().getStreetNo()).orElse("") : "",
                        ci.getAddress() != null ? Optional.ofNullable(ci.getAddress().getPlz()).orElse("") : "",
                        ci.getAddress() != null ? Optional.ofNullable(ci.getAddress().getCity()).orElse("") : ""
                ).trim();

        String body = """
                Order CANCELLED — paid via Stripe (manual refund needed)
                
                Kind:    %s
                OrderID: %s
                
                Reason:  %s
                
                Customer:
                %s
                """.formatted(kind, id, Optional.ofNullable(reason).orElse("(none)"), customerBlock);

        mailService.sendSimple(otherOwnerEmail, "Refund needed — " + kind + " " + id, body, null);
    }
}