package ch.asiankitchen.service;

import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    // ── Repos
    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;

    // ── Domain services
    private final DiscountService discountService;
    private final WebPushService webPushService;

    // Add these so we can send the customer confirmation on webhook success for card
    private final CustomerOrderService customerOrderService;
    private final BuffetOrderService buffetOrderService;

    // ── Config
    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:chf}")
    private String currency;

    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    @Value("${app.delivery.allowed-plz:}")
    private String allowedPlzCsv;

    @Value("${app.delivery.reject-message:We don’t deliver to this address.}")
    private String rejectMessage;

    @Value("${app.delivery.min-order-chf:30.00}")
    private BigDecimal minOrderChf;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal deliveryFeeChf;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeDeliveryThresholdChf;

    private Set<String> allowedPlz;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
        allowedPlz = Arrays.stream(allowedPlzCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // ───────────────────────────────────────────────────────────
    // CUSTOMER ORDER
    // ───────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForCustomerOrder(UUID id) throws StripeException {
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        if (order.getPaymentMethod() == PaymentMethod.CASH
                || order.getPaymentMethod() == PaymentMethod.POS_CARD
                || order.getPaymentMethod() == PaymentMethod.TWINT) {
            throw new IllegalArgumentException("This payment method does not require a payment intent.");
        }

        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // Server-truth items subtotal from DB prices
        BigDecimal items = order.getOrderItems().stream()
                .map(oi -> Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (order.getOrderType() == OrderType.DELIVERY) {
            enforceMinOrder(items);
        }

        // Discount → VAT → Delivery → Grand
        DiscountResult dr = applyDiscountMenu(items);
        BigDecimal vat = calcVat(order.getOrderType(), dr.discountedItems());
        BigDecimal delivery = calcDeliveryFee(order.getOrderType(), dr.discountedItems());
        BigDecimal grand = dr.discountedItems().add(vat).add(delivery).setScale(2, RoundingMode.HALF_UP);

        // Snapshot on entity
        order.setItemsSubtotalBeforeDiscount(items);
        order.setDiscountPercent(dr.discountPercent());
        order.setDiscountAmount(dr.discountAmount());
        order.setItemsSubtotalAfterDiscount(dr.discountedItems());
        order.setVatAmount(vat);
        order.setDeliveryFee(delivery);
        order.setTotalPrice(grand);
        customerOrderRepo.save(order);

        long minor = toMinor(grand);
        enforceStripeMinimum(minor);

        PaymentIntent intent = createStripeIntent(minor, "customer", order.getId(), order.getPaymentMethod());
        order.setPaymentIntentId(intent.getId());
        order.setPaymentStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        customerOrderRepo.save(order);

        return intent;
    }

    // ───────────────────────────────────────────────────────────
    // BUFFET ORDER
    // ───────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        if (order.getPaymentMethod() == PaymentMethod.CASH
                || order.getPaymentMethod() == PaymentMethod.POS_CARD
                || order.getPaymentMethod() == PaymentMethod.TWINT) {
            throw new IllegalArgumentException("This payment method does not require a payment intent.");
        }

        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // BuffetOrder.totalPrice is the sum of items (see entity recalc) → treat as items pre-discount
        BigDecimal items = Optional.ofNullable(order.getTotalPrice()).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        if (order.getOrderType() == OrderType.DELIVERY) {
            enforceMinOrder(items);
        }

        DiscountResult dr = applyDiscountBuffet(items);
        BigDecimal vat = calcVat(order.getOrderType(), dr.discountedItems());
        BigDecimal delivery = calcDeliveryFee(order.getOrderType(), dr.discountedItems());
        BigDecimal grand = dr.discountedItems().add(vat).add(delivery).setScale(2, RoundingMode.HALF_UP);

        // Snapshot on entity
        order.setItemsSubtotalBeforeDiscount(items);
        order.setDiscountPercent(dr.discountPercent());
        order.setDiscountAmount(dr.discountAmount());
        order.setItemsSubtotalAfterDiscount(dr.discountedItems());
        order.setVatAmount(vat);
        order.setDeliveryFee(delivery);
        order.setTotalPrice(grand);
        buffetOrderRepo.save(order);

        long minor = toMinor(grand);
        enforceStripeMinimum(minor);

        PaymentIntent intent = createStripeIntent(minor, "buffet", order.getId(), order.getPaymentMethod());
        order.setPaymentIntentId(intent.getId());
        order.setPaymentStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        buffetOrderRepo.save(order);

        return intent;
    }

    // ───────────────────────────────────────────────────────────
    // WEBHOOK HANDLER (robust with metadata fallback + email on success)
    // ───────────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        System.out.println("[WEBHOOK] type=" + event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    System.out.println("[WEBHOOK] PI success id=" + pi.getId());
                    updateFromStripeObjects(pi, null, PaymentStatus.SUCCEEDED, null);
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    System.out.println("[WEBHOOK] PI failed id=" + pi.getId());
                    updateFromStripeObjects(pi, null, PaymentStatus.FAILED, null);
                }
            }
            case "charge.succeeded" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null) {
                    System.out.println("[WEBHOOK] Charge success pi=" + ch.getPaymentIntent());
                    updateFromStripeObjects(null, ch, PaymentStatus.SUCCEEDED, resolveMethod(ch));
                }
            }
            case "charge.failed" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null) {
                    System.out.println("[WEBHOOK] Charge failed pi=" + ch.getPaymentIntent());
                    updateFromStripeObjects(null, ch, PaymentStatus.FAILED, resolveMethod(ch));
                }
            }
            default -> { /* ignore others */ }
        }
    }

    private void updateFromStripeObjects(PaymentIntent pi,
                                         Charge ch,
                                         PaymentStatus status,
                                         PaymentMethod maybeMethod) {
        // If we only received a Charge, try to load the PI it references
        if (pi == null && ch != null && ch.getPaymentIntent() != null) {
            try { pi = PaymentIntent.retrieve(ch.getPaymentIntent()); } catch (Exception ignored) {}
        }

        String piId = (pi != null) ? pi.getId() : (ch != null ? ch.getPaymentIntent() : null);
        Map<String, String> md = (pi != null && pi.getMetadata() != null) ? pi.getMetadata() : Collections.emptyMap();

        boolean touched = false;

        // 1) Primary: match by payment_intent_id stored on order
        if (piId != null) {
            var co = customerOrderRepo.findByPaymentIntentId(piId);
            if (co.isPresent()) {
                touched = true;
                applyUpdateToCustomer(co.get(), status, maybeMethod, true);
            }
            var bo = buffetOrderRepo.findByPaymentIntentId(piId);
            if (bo.isPresent()) {
                touched = true;
                applyUpdateToBuffet(bo.get(), status, maybeMethod, true);
            }
        }

        // 2) Fallback: use your own metadata.orderId + metadata.type (customer|buffet)
        if (!touched && md.containsKey("orderId")) {
            try {
                UUID orderId = UUID.fromString(md.get("orderId"));
                String kind = md.getOrDefault("type", "customer");
                if ("buffet".equalsIgnoreCase(kind)) {
                    buffetOrderRepo.findById(orderId).ifPresent(o -> {
                        // Backfill PI if missing/mismatched
                        if (piId != null && (o.getPaymentIntentId() == null || o.getPaymentIntentId().isBlank())) {
                            o.setPaymentIntentId(piId);
                        }
                        applyUpdateToBuffet(o, status, maybeMethod, false);
                    });
                    touched = true;
                } else {
                    customerOrderRepo.findById(orderId).ifPresent(o -> {
                        if (piId != null && (o.getPaymentIntentId() == null || o.getPaymentIntentId().isBlank())) {
                            o.setPaymentIntentId(piId);
                        }
                        applyUpdateToCustomer(o, status, maybeMethod, false);
                    });
                    touched = true;
                }
            } catch (IllegalArgumentException ignored) {
                // Bad UUID string — ignore
            }
        }

        if (!touched && piId != null) {
            System.out.println("[WEBHOOK] No order matched by PI or metadata. PI=" + piId);
        }
    }

    private void applyUpdateToCustomer(CustomerOrder o,
                                       PaymentStatus status,
                                       PaymentMethod maybeMethod,
                                       boolean matchedByPi) {
        PaymentStatus prev = o.getPaymentStatus();
        o.setPaymentStatus(status);
        if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
        if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.CONFIRMED);
        }
        customerOrderRepo.save(o);

        // On first transition to SUCCEEDED, send signals
        if (status == PaymentStatus.SUCCEEDED && prev != PaymentStatus.SUCCEEDED) {
            try {
                webPushService.broadcast("admin",
                        """
                        {"title":"New Order (Menu)","body":"Paid %s order %s","url":"/admin/orders"}
                        """.formatted(o.getOrderType(), o.getId()));
            } catch (Exception ignored) {}

            // Send customer email for card payments once webhook confirms success
            try {
                if (o.getPaymentMethod() == PaymentMethod.CARD) {
                    customerOrderService.sendCustomerConfirmationWithTrackLink(o);
                }
            } catch (Exception ignored) {}
        }

        System.out.println("[WEBHOOK] Updated CUSTOMER order " + o.getId()
                + " via " + (matchedByPi ? "payment_intent_id" : "metadata"));
    }

    private void applyUpdateToBuffet(BuffetOrder o,
                                     PaymentStatus status,
                                     PaymentMethod maybeMethod,
                                     boolean matchedByPi) {
        PaymentStatus prev = o.getPaymentStatus();
        o.setPaymentStatus(status);
        if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
        if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.CONFIRMED);
        }
        buffetOrderRepo.save(o);

        if (status == PaymentStatus.SUCCEEDED && prev != PaymentStatus.SUCCEEDED) {
            try {
                webPushService.broadcast("admin",
                        """
                        {"title":"New Order (Buffet)","body":"Paid %s order %s","url":"/admin/buffet-orders"}
                        """.formatted(o.getOrderType(), o.getId()));
            } catch (Exception ignored) {}

            try {
                if (o.getPaymentMethod() == PaymentMethod.CARD) {
                    buffetOrderService.sendCustomerConfirmationWithTrackLink(o);
                }
            } catch (Exception ignored) {}
        }

        System.out.println("[WEBHOOK] Updated BUFFET order " + o.getId()
                + " via " + (matchedByPi ? "payment_intent_id" : "metadata"));
    }

    private PaymentMethod resolveMethod(Charge ch) {
        try {
            String type = ch.getPaymentMethodDetails() != null
                    ? ch.getPaymentMethodDetails().getType()
                    : null;
            if ("twint".equalsIgnoreCase(type)) return PaymentMethod.TWINT;
            return PaymentMethod.CARD;
        } catch (Exception e) {
            return PaymentMethod.CARD;
        }
    }

    // ───────────────────────────────────────────────────────────
    // DISCOUNT HELPERS
    // ───────────────────────────────────────────────────────────
    private DiscountResult applyDiscountMenu(BigDecimal itemsSubtotal) {
        var active = discountService.resolveActive();
        BigDecimal pct = active.percentMenu();
        BigDecimal rate = pct.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal discount = itemsSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discounted = itemsSubtotal.subtract(discount).max(BigDecimal.ZERO);
        return new DiscountResult(discounted, discount, pct);
    }

    private DiscountResult applyDiscountBuffet(BigDecimal itemsSubtotal) {
        var active = discountService.resolveActive();
        BigDecimal pct = active.percentBuffet();
        BigDecimal rate = pct.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal discount = itemsSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discounted = itemsSubtotal.subtract(discount).max(BigDecimal.ZERO);
        return new DiscountResult(discounted, discount, pct);
    }

    private record DiscountResult(BigDecimal discountedItems, BigDecimal discountAmount, BigDecimal discountPercent) {}

    // ───────────────────────────────────────────────────────────
    // TOTAL HELPERS
    // ───────────────────────────────────────────────────────────
    private void enforceDeliveryZone(OrderType orderType, CustomerInfo info) {
        if (orderType != OrderType.DELIVERY) return;
        String plz = Optional.ofNullable(info)
                .map(CustomerInfo::getAddress)
                .map(Address::getPlz)
                .map(String::trim)
                .orElse("");
        if (!allowedPlz.contains(plz)) {
            throw new IllegalArgumentException(rejectMessage);
        }
    }

    private void enforceMinOrder(BigDecimal itemsPreDiscount) {
        if (itemsPreDiscount.compareTo(minOrderChf) < 0) {
            throw new IllegalArgumentException(
                    "Minimum delivery order is CHF " + minOrderChf.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal calcVat(OrderType orderType, BigDecimal itemsSubtotalUsed) {
        boolean taxable = (orderType == OrderType.TAKEAWAY || orderType == OrderType.DELIVERY);
        if (!taxable) return BigDecimal.ZERO;
        BigDecimal rate = vatRatePercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return itemsSubtotalUsed.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDeliveryFee(OrderType orderType, BigDecimal itemsSubtotalUsed) {
        if (orderType != OrderType.DELIVERY) return BigDecimal.ZERO;
        if (itemsSubtotalUsed.compareTo(freeDeliveryThresholdChf) >= 0) {
            return BigDecimal.ZERO;
        }
        return deliveryFeeChf.setScale(2, RoundingMode.HALF_UP);
    }

    private long toMinor(BigDecimal amountChf) {
        return amountChf.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void enforceStripeMinimum(long amountMinor) {
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }
    }

    private PaymentIntent createStripeIntent(long amountMinor,
                                             String orderTypeMeta,
                                             UUID orderId,
                                             PaymentMethod intended) throws StripeException {
        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .putMetadata("type", orderTypeMeta)
                .putMetadata("orderId", orderId.toString())
                .putMetadata("intendedMethod", String.valueOf(intended));
        return PaymentIntent.create(builder.build());
    }
}