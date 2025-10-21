package ch.asiankitchen.service;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.dto.CustomerOrderWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository repo;
    private final MenuItemRepository menuItemRepo;
    private final EmailService email;
    private final WebPushService webPushService;
    private final DiscountService discountService;
    private final OrderWorkflowService workflow;

    public CustomerOrderService(CustomerOrderRepository repo,
                                MenuItemRepository menuItemRepo,
                                EmailService email,
                                WebPushService webPushService,
                                DiscountService discountService,
                                OrderWorkflowService workflow) {
        this.repo = repo;
        this.menuItemRepo = menuItemRepo;
        this.email = email;
        this.webPushService = webPushService;
        this.discountService = discountService;
        this.workflow = workflow;
    }

    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal deliveryFeeChf;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeDeliveryThresholdChf;

    @Value("${app.timezone:Europe/Zurich}")
    private String appTz;

    /* ---------- time utils ---------- */
    private DateTimeFormatter fmt() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    private String localFmt(LocalDateTime utc) {
        if (utc == null) return "—";
        return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of(appTz)).format(fmt());
    }

    @Transactional
    public CustomerOrderReadDTO create(CustomerOrderWriteDTO dto) {
        final CustomerOrder order = dto.toEntity();
        order.setStatus(OrderStatus.NEW);

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        order.getOrderItems().forEach(oi -> {
            if (oi.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each item must have a quantity of at least 1.");
            }
            UUID id = oi.getMenuItem().getId();
            MenuItem mi = menuItemRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));

            if (!mi.isAvailable()) {
                String itemName = Optional.ofNullable(mi.getFoodItem())
                        .map(FoodItem::getName).orElse("Item " + mi.getId());
                throw new IllegalArgumentException("Menu item not available: " + itemName);
            }

            oi.setMenuItem(mi);
            oi.setCustomerOrder(order);
        });

        BigDecimal items = order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal unit = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
                    if (unit.compareTo(BigDecimal.ZERO) <= 0) {
                        String nm = Optional.ofNullable(oi.getMenuItem().getFoodItem())
                                .map(FoodItem::getName).orElse("Item " + oi.getMenuItem().getId());
                        throw new IllegalArgumentException("Price missing or zero for: " + nm);
                    }
                    return unit.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var dr = discountForMenu(items);
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

        CustomerOrder saved = repo.save(order);

        workflow.applyInitialTiming(saved);
        saved = repo.save(saved);

        if (saved.getPaymentMethod() != PaymentMethod.CARD) {
            try {
                webPushService.broadcast("admin",
                        """
                                {"title":"New Order (Menu)","body":"%s order %s","url":"/admin/orders"}
                                """.formatted(saved.getOrderType(), saved.getId()));
            } catch (Throwable ignored) {
            }
            try {
                sendCustomerConfirmationWithTrackLink(saved);
            } catch (Throwable ignored) {
            }
        }

        return CustomerOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
        order.setStatus(status);
        return CustomerOrderReadDTO.fromEntity(repo.save(order));
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO track(UUID id, String emailAddr) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(emailAddr))
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listAllVisibleForAdmin() {
        var statuses = List.of(
                PaymentStatus.SUCCEEDED,
                PaymentStatus.NOT_REQUIRED);
        return repo.findAdminVisibleWithItems(statuses)
                .stream().map(CustomerOrderReadDTO::fromEntity).toList();
    }

    /**
     * Send when (a) non-card order created, or (b) Stripe webhook success for card.
     */
    public void sendCustomerConfirmationWithTrackLink(CustomerOrder order) {
        final String to = order.getCustomerInfo() != null ? order.getCustomerInfo().getEmail() : null;
        if (to == null || to.isBlank()) return;

        String trackUrl = "https://asian-kitchen.online/track?orderId=%s&email=%s"
                .formatted(order.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

        String placedLocal = localFmt(order.getCreatedAt());
        String deliverLocal = order.isAsap()
                ? "ASAP"
                : localFmt(order.getCommittedReadyAt());

        String subject = "Your order at Asian Kitchen";
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

    /* ---------------- helpers ---------------- */
    private record Discount(BigDecimal discountedItems, BigDecimal amount, BigDecimal percent) {
    }

    private Discount discountForMenu(BigDecimal itemsSubtotal) {
        var active = discountService.resolveActive(); // never null
        BigDecimal pct = active.percentMenu();        // never null
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