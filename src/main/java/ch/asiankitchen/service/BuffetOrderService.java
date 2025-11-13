package ch.asiankitchen.service;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.dto.BuffetOrderWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BuffetOrderService {

    private final BuffetOrderRepository repo;
    private final EmailService email;
    private final WebPushService webPushService;
    private final DiscountService discountService;
    private final OrderWorkflowService workflow;
    private final HoursService hoursService;

    public BuffetOrderService(BuffetOrderRepository repo,
                              EmailService email,
                              WebPushService webPushService,
                              DiscountService discountService,
                              OrderWorkflowService workflow,
                              HoursService hoursService) {
        this.repo = repo;
        this.email = email;
        this.webPushService = webPushService;
        this.discountService = discountService;
        this.workflow = workflow;
        this.hoursService = hoursService;
    }

    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal deliveryFeeChf;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeDeliveryThresholdChf;

    @Value("${app.timezone:Europe/Zurich}")
    private String appTz;

    private DateTimeFormatter fmt() { return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); }
    private String localFmt(LocalDateTime utc) {
        if (utc == null) return "—";
        return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of(appTz)).format(fmt());
    }

    private Instant toAppInstant(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneId.of(appTz)).toInstant();
    }

    @Transactional
    public BuffetOrderReadDTO create(BuffetOrderWriteDTO dto) {
        final BuffetOrder order = dto.toEntity();
        order.setStatus(OrderStatus.NEW);

        final boolean forDelivery = order.getOrderType() == OrderType.DELIVERY;
        final boolean asap = Boolean.TRUE.equals(order.isAsap());
        final Instant scheduledAt = !asap ? toAppInstant(dto.getScheduledAt()) : null;

        hoursService.assertOrderAllowed(forDelivery, asap, scheduledAt);

        BigDecimal items = Optional.ofNullable(order.getTotalPrice())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        var dr = discountForBuffet(items);
        BigDecimal vat = calcVat(order.getOrderType(), dr.discountedItems());
        BigDecimal delivery = calcDelivery(order.getOrderType(), dr.discountedItems());
        BigDecimal grand = dr.discountedItems().add(vat).add(delivery).setScale(2, RoundingMode.HALF_UP);

        order.setItemsSubtotalBeforeDiscount(items);
        order.setDiscountPercent(dr.percent());
        order.setDiscountAmount(dr.amount());
        order.setItemsSubtotalAfterDiscount(dr.discountedItems());
        order.setVatAmount(vat);
        order.setDeliveryFee(delivery);
        order.setTotalPrice(grand);

        if (order.getPaymentMethod() != PaymentMethod.CARD) {
            order.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
        }

        BuffetOrder saved = repo.save(order);
        workflow.applyInitialTiming(saved);
        saved = repo.save(saved);

        if (saved.getPaymentMethod() != PaymentMethod.CARD) {
            try {
                webPushService.broadcast(
                        "admin",
                        """
                        {"title":"New Order (Buffet)","body":"%s order %s","url":"/admin/buffet-orders"}
                        """.formatted(saved.getOrderType(), saved.getId())
                );
            } catch (Throwable ignored) {}
            try { sendCustomerConfirmationWithTrackLink(saved); } catch (Throwable ignored) {}
        }

        return BuffetOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listAllVisibleForAdmin() {
        var statuses = List.of(PaymentStatus.SUCCEEDED, PaymentStatus.NOT_REQUIRED);
        return repo.findAdminVisibleWithItems(statuses).stream().map(BuffetOrderReadDTO::fromEntity).toList();
    }

    @Transactional
    public BuffetOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
        order.setStatus(status);
        return BuffetOrderReadDTO.fromEntity(repo.save(order));
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO track(UUID id, String emailAddr) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(emailAddr))
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    /** Send when (a) non-card buffet order created, or (b) Stripe webhook success for card buffet. */
    public void sendCustomerConfirmationWithTrackLink(BuffetOrder order) {
        final String to = order.getCustomerInfo() != null ? order.getCustomerInfo().getEmail() : null;
        if (to == null || to.isBlank()) return;

        String trackUrl = "https://asian-kitchen.online/track-buffet?orderId=%s&email=%s"
                .formatted(order.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

        String placedLocal = localFmt(order.getCreatedAt());
        String deliverLocal = order.isAsap() ? "ASAP" : localFmt(order.getCommittedReadyAt());

        String subject = "Your buffet order at Asian Kitchen";
        String body = """
                Hi %s,

                Thanks for your %s!

                Placed:  %s
                Deliver: %s

                Track your order:
                %s

                Order ID: %s
                Total: CHF %s

                — Asian Kitchen
                """.formatted(
                Optional.ofNullable(order.getCustomerInfo()).map(CustomerInfo::getFirstName).orElse(""),
                order.getPaymentMethod() == PaymentMethod.CARD ? "payment" : "order",
                placedLocal,
                deliverLocal,
                trackUrl,
                order.getId(),
                order.getTotalPrice()
        );

        email.sendSimple(to, subject, body, null);
    }

    /* ---------- helpers ---------- */
    private record Discount(BigDecimal discountedItems, BigDecimal amount, BigDecimal percent) {}

    private Discount discountForBuffet(BigDecimal itemsSubtotal) {
        var active = discountService.resolveActive();
        BigDecimal pct = active.percentBuffet();
        BigDecimal rate = pct.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal discount = itemsSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discounted = itemsSubtotal.subtract(discount).max(BigDecimal.ZERO);
        return new Discount(discounted, discount, pct);
    }

    private BigDecimal calcVat(OrderType orderType, BigDecimal itemsAfterDiscount) {
        boolean taxable = (orderType == OrderType.TAKEAWAY || orderType == OrderType.DELIVERY);
        if (!taxable) return BigDecimal.ZERO;
        BigDecimal rate = vatRatePercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return itemsAfterDiscount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDelivery(OrderType orderType, BigDecimal itemsAfterDiscount) {
        if (orderType != OrderType.DELIVERY) return BigDecimal.ZERO;
        if (itemsAfterDiscount.compareTo(freeDeliveryThresholdChf) >= 0) return BigDecimal.ZERO;
        return deliveryFeeChf.setScale(2, RoundingMode.HALF_UP);
    }
}
