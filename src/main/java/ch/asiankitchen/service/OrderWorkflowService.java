package ch.asiankitchen.service;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /** Where to notify the co-owner/escalation recipient (refunds, auto-cancels, etc.). */
    @Value("${app.mail.to.escalation:}")
    private String escalationEmail;

    /* ------------------------------ time helpers ------------------------------ */

    private DateTimeFormatter fmt() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    /** Convert a UTC LocalDateTime -> formatted App TZ string. */
    private String localFmt(@Nullable LocalDateTime utc) {
        if (utc == null) return "—";
        return utc.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(appTz))
                .format(fmt());
    }

    /** Convert a local (app TZ) LocalDateTime -> UTC LocalDateTime. */
    private LocalDateTime toUtc(@Nullable LocalDateTime local) {
        if (local == null) return null;
        return local.atZone(ZoneId.of(appTz))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private int etaMinutes(CustomerOrder o) {
        int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
        int extra = Optional.ofNullable(o.getAdminExtraMinutes()).orElse(0);
        return Math.max(0, minPrep + extra);
    }

    private int etaMinutes(BuffetOrder o) {
        int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
        int extra = Optional.ofNullable(o.getAdminExtraMinutes()).orElse(0);
        return Math.max(0, minPrep + extra);
    }

    /* ------------------------------ initial timing ------------------------------ */

    // compute committed times on creation (and the alert window)
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

    /* ------------------------------ admin interactions ------------------------------ */

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

    /**
     * Update admin extra minutes for ASAP NEW orders (menu/buffet). Recomputes committedReadyAt.
     */
    @Transactional
    public void patchExtraMinutes(String kind, UUID id, int extra) {
        final int add = Math.max(0, extra);
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(minPrep + add));
                    customerOrderRepo.save(o);
                }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(minPrep + add));
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

    /**
     * Confirm order/reservation. For ASAP menu/buffet: applies extraMinutes before confirming,
     * recomputes committedReadyAt, then emails customer with ETA and a tracking link.
     */
    @Transactional
    public void confirmOrder(String kind, UUID id, @Nullable Integer extraMinutes, boolean print) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                // apply extra minutes first (ASAP + NEW)
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW && extraMinutes != null && extraMinutes >= 0) {
                    o.setAdminExtraMinutes(extraMinutes);
                    int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(minPrep + extraMinutes));
                }
                o.setStatus(OrderStatus.CONFIRMED);
                customerOrderRepo.save(o);

                // notify admins
                try { webPushService.broadcast("admin", "{\"title\":\"Order confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}

                // customer email: ETA + approx local time + track link
                try {
                    final String to = o.getCustomerInfo() != null ? o.getCustomerInfo().getEmail() : null;
                    if (to != null && !to.isBlank()) {
                        String trackUrl = "https://asian-kitchen.online/track?orderId=%s&email=%s"
                                .formatted(o.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

                        int eta = etaMinutes(o);
                        String approxTime = localFmt(o.getCommittedReadyAt());

                        String subject = "Your order is confirmed — ETA ~" + eta + " min";
                        String body = """
                                Hi %s,

                                Your order is confirmed. We will deliver in approximately %d minutes (around %s).

                                Track your order:
                                %s

                                Order ID: %s
                                Total: CHF %s

                                — Asian Kitchen
                                """.formatted(
                                Optional.ofNullable(o.getCustomerInfo().getFirstName()).orElse(""),
                                eta, approxTime, trackUrl, o.getId(), o.getTotalPrice()
                        );
                        mailService.sendSimple(to, subject, body, null);
                    }
                } catch (Exception e) {
                    log.warn("MAIL: failed sending confirm email for order {}", o.getId(), e);
                }

                if (print) {
                    // optional: integrate printer hook here if needed
                }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW && extraMinutes != null && extraMinutes >= 0) {
                    o.setAdminExtraMinutes(extraMinutes);
                    int minPrep = Optional.ofNullable(o.getMinPrepMinutes()).orElse(defaultMinPrep);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(minPrep + extraMinutes));
                }
                o.setStatus(OrderStatus.CONFIRMED);
                buffetOrderRepo.save(o);

                try { webPushService.broadcast("admin", "{\"title\":\"Buffet confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}

                try {
                    final String to = o.getCustomerInfo() != null ? o.getCustomerInfo().getEmail() : null;
                    if (to != null && !to.isBlank()) {
                        String trackUrl = "https://asian-kitchen.online/track-buffet?orderId=%s&email=%s"
                                .formatted(o.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

                        int eta = etaMinutes(o);
                        String approxTime = localFmt(o.getCommittedReadyAt());

                        String subject = "Your buffet order is confirmed — ETA ~" + eta + " min";
                        String body = """
                                Hi %s,

                                Your buffet order is confirmed. We will deliver in approximately %d minutes (around %s).

                                Track your order:
                                %s

                                Order ID: %s
                                Total: CHF %s

                                — Asian Kitchen
                                """.formatted(
                                Optional.ofNullable(o.getCustomerInfo().getFirstName()).orElse(""),
                                eta, approxTime, trackUrl, o.getId(), o.getTotalPrice()
                        );
                        mailService.sendSimple(to, subject, body, null);
                    }
                } catch (Exception e) {
                    log.warn("MAIL: failed sending confirm email for buffet {}", o.getId(), e);
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

    /**
     * Cancel entities. If a paid (Stripe SUCCEEDED) order is cancelled,
     * email the co-owner with refund details and customer info.
     */
    @Transactional
    public void cancelOrder(String kind, UUID id, String reason, boolean refundIfPaid) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                customerOrderRepo.save(o);

                // Optional: notify customer here if desired

                // Refund notification to co-owner for paid orders
                if (o.getPaymentStatus() == PaymentStatus.SUCCEEDED && escalationEmailConfigured()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Customer (paid via Stripe):\n");
                    if (o.getCustomerInfo() != null) {
                        sb.append(Optional.ofNullable(o.getCustomerInfo().getFirstName()).orElse("")).append(" ")
                                .append(Optional.ofNullable(o.getCustomerInfo().getLastName()).orElse("")).append("\n");
                        sb.append(Optional.ofNullable(o.getCustomerInfo().getEmail()).orElse("")).append("\n");
                        sb.append(Optional.ofNullable(o.getCustomerInfo().getPhone()).orElse("")).append("\n");
                    }
                    sb.append("\nOrder ID: ").append(o.getId()).append("\n");
                    sb.append("Total: CHF ").append(o.getTotalPrice()).append("\n");
                    sb.append("Reason: ").append(reason == null ? "" : reason).append("\n");

                    sb.append("\nItems:\n");
                    if (o.getOrderItems() != null) {
                        o.getOrderItems().forEach(it -> {
                            String nm = Optional.ofNullable(it.getMenuItem())
                                    .map(mi -> Optional.ofNullable(mi.getFoodItem()).map(FoodItem::getName).orElse(""))
                                    .filter(s -> !s.isBlank())
                                    .orElse(Optional.ofNullable(it.getMenuItem().getFoodItem().getName()).orElse("Item"));
                            sb.append(" - ").append(nm).append(" x ").append(it.getQuantity()).append("\n");
                        });
                    }

                    try {
                        mailService.sendSimple(escalationEmail,
                                "Refund needed — cancelled paid order " + o.getId(),
                                sb.toString(),
                                null);
                    } catch (Exception e) {
                        log.warn("MAIL: failed sending refund email for order {}", o.getId(), e);
                    }
                }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                buffetOrderRepo.save(o);

                if (o.getPaymentStatus() == PaymentStatus.SUCCEEDED && escalationEmailConfigured()) {
                    String body = """
                            Customer (paid via Stripe):
                            %s %s
                            %s
                            %s

                            Buffet Order ID: %s
                            Total: CHF %s
                            Reason: %s
                            """.formatted(
                            Optional.ofNullable(o.getCustomerInfo()).map(ci -> Optional.ofNullable(ci.getFirstName()).orElse("")).orElse(""),
                            Optional.ofNullable(o.getCustomerInfo()).map(ci -> Optional.ofNullable(ci.getLastName()).orElse("")).orElse(""),
                            Optional.ofNullable(o.getCustomerInfo()).map(ci -> Optional.ofNullable(ci.getEmail()).orElse("")).orElse(""),
                            Optional.ofNullable(o.getCustomerInfo()).map(ci -> Optional.ofNullable(ci.getPhone()).orElse("")).orElse(""),
                            o.getId(), o.getTotalPrice(), reason == null ? "" : reason
                    );
                    try {
                        mailService.sendSimple(escalationEmail,
                                "Refund needed — cancelled paid buffet " + o.getId(),
                                body,
                                null);
                    } catch (Exception e) {
                        log.warn("MAIL: failed sending refund email for buffet {}", o.getId(), e);
                    }
                }
            });
        } else if ("reservation".equals(kind)) {
            reservationRepo.findById(id).ifPresent(r -> {
                r.setStatus(ReservationStatus.CANCELLED);
                reservationRepo.save(r);
                try { reservationEmailService.sendRejectionToCustomer(r); } catch (Exception ignored) {}
            });
        }
    }

    private boolean escalationEmailConfigured() {
        return escalationEmail != null && !escalationEmail.isBlank();
    }
}