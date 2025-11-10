package ch.asiankitchen.service;

import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
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

    // ── Services
    private final DiscountService discountService;
    private final WebPushService webPushService;
    private final DeliveryZoneService deliveryZones;
    // used to send customer email after webhook success
    private final CustomerOrderService customerOrderService;
    private final BuffetOrderService buffetOrderService;

    private final OrderWorkflowService workflow;

    // ── Config
    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:chf}")
    private String currency;

    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    @Value("${app.delivery.reject-message:We don’t deliver to this address.}")
    private String rejectMessage;

    @Value("${app.delivery.min-order-chf:30.00}")
    private BigDecimal minOrderChf;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal deliveryFeeChf;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeDeliveryThresholdChf;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
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

        workflow.applyInitialTiming(order);
        customerOrderRepo.save(order);

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

        workflow.applyInitialTiming(order);
        buffetOrderRepo.save(order);

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
    // WEBHOOK HANDLER (resilient to API version changes)
    // ───────────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        System.out.println("[WEBHOOK] type=" + event.getType());

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent pi = deserializePaymentIntent(event.getDataObjectDeserializer());
                    if (pi != null) {
                        System.out.println("[WEBHOOK] PI success id=" + pi.getId());
                        updateByPaymentIntent(pi.getId(), PaymentStatus.SUCCEEDED, null);
                    } else {
                        System.out.println("[WEBHOOK] PI deserialization FAILED");
                    }
                }
                case "payment_intent.payment_failed" -> {
                    PaymentIntent pi = deserializePaymentIntent(event.getDataObjectDeserializer());
                    if (pi != null) {
                        System.out.println("[WEBHOOK] PI failed id=" + pi.getId());
                        updateByPaymentIntent(pi.getId(), PaymentStatus.FAILED, null);
                    } else {
                        System.out.println("[WEBHOOK] PI deserialization FAILED");
                    }
                }
                case "charge.succeeded" -> {
                    Charge ch = deserializeCharge(event.getDataObjectDeserializer());
                    if (ch != null && ch.getPaymentIntent() != null) {
                        System.out.println("[WEBHOOK] Charge success pi=" + ch.getPaymentIntent());
                        updateByPaymentIntent(ch.getPaymentIntent(), PaymentStatus.SUCCEEDED, resolveMethod(ch));
                    } else {
                        System.out.println("[WEBHOOK] Charge deserialization FAILED or missing PI");
                    }
                }
                case "charge.failed" -> {
                    Charge ch = deserializeCharge(event.getDataObjectDeserializer());
                    if (ch != null && ch.getPaymentIntent() != null) {
                        System.out.println("[WEBHOOK] Charge failed pi=" + ch.getPaymentIntent());
                        updateByPaymentIntent(ch.getPaymentIntent(), PaymentStatus.FAILED, resolveMethod(ch));
                    } else {
                        System.out.println("[WEBHOOK] Charge deserialization FAILED or missing PI");
                    }
                }
                default -> { /* ignore others */ }
            }
        } catch (Exception e) {
            // log but do not 5xx, to avoid Stripe retries if we already handled it
            System.out.println("[WEBHOOK] Handler exception: " + e.getMessage());
        }
    }

    /** Try SDK deserialization; if it fails, parse raw JSON to get id and retrieve from Stripe. */
    private PaymentIntent deserializePaymentIntent(EventDataObjectDeserializer d) {
        return d.getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElseGet(() -> {
                    try {
                        String raw = d.getRawJson();
                        if (raw == null) return null;
                        JsonObject jo = JsonParser.parseString(raw).getAsJsonObject();
                        String id = jo.has("id") ? jo.get("id").getAsString() : null;
                        return (id != null) ? PaymentIntent.retrieve(id) : null;
                    } catch (Exception ignored) {
                        return null;
                    }
                });
    }

    private Charge deserializeCharge(EventDataObjectDeserializer d) {
        return d.getObject()
                .filter(Charge.class::isInstance)
                .map(Charge.class::cast)
                .orElseGet(() -> {
                    try {
                        String raw = d.getRawJson();
                        if (raw == null) return null;
                        JsonObject jo = JsonParser.parseString(raw).getAsJsonObject();
                        String id = jo.has("id") ? jo.get("id").getAsString() : null;
                        return (id != null) ? Charge.retrieve(id) : null;
                    } catch (Exception ignored) {
                        return null;
                    }
                });
    }

    // ───────────────────────────────────────────────────────────
    // PERSISTENCE UPDATE (with metadata fallback + email + push)
    // ───────────────────────────────────────────────────────────
    private void updateByPaymentIntent(String paymentIntentId,
                                       PaymentStatus status,
                                       PaymentMethod maybeMethod) {
        System.out.println("[WEBHOOK] Updating by PI=" + paymentIntentId + " -> " + status);

        boolean touched = false;

        var co = customerOrderRepo.findByPaymentIntentId(paymentIntentId);
        if (co.isPresent()) {
            touched = true;
            var o = co.get();
            System.out.println("[WEBHOOK] Matched CUSTOMER order " + o.getId());
            o.setPaymentStatus(status);
            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            customerOrderRepo.save(o);

            if (status == PaymentStatus.SUCCEEDED) {
                try {
                    webPushService.broadcast("admin",
                            """
                            {"title":"New Order (Menu)","body":"Paid %s order %s","url":"/admin/orders"}
                            """.formatted(o.getOrderType(), o.getId()));
                } catch (Exception ignored) {}
                try {
                    customerOrderService.sendCustomerConfirmationWithTrackLink(o);
                } catch (Exception ignored) {}
            }
        }

        var bo = buffetOrderRepo.findByPaymentIntentId(paymentIntentId);
        if (bo.isPresent()) {
            touched = true;
            var o = bo.get();
            System.out.println("[WEBHOOK] Matched BUFFET order " + o.getId());
            o.setPaymentStatus(status);
            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            buffetOrderRepo.save(o);

            if (status == PaymentStatus.SUCCEEDED) {
                try {
                    webPushService.broadcast("admin",
                            """
                            {"title":"New Order (Buffet)","body":"Paid %s order %s","url":"/admin/buffet-orders"}
                            """.formatted(o.getOrderType(), o.getId()));
                } catch (Exception ignored) {}
                try {
                    buffetOrderService.sendCustomerConfirmationWithTrackLink(o);
                } catch (Exception ignored) {}
            }
        }

        // If not matched by PI, try metadata fallback (orderId/type) and backfill payment_intent_id
        if (!touched) {
            try {
                PaymentIntent pi = PaymentIntent.retrieve(paymentIntentId);
                Map<String, String> md = pi.getMetadata();
                String orderId = md != null ? md.get("orderId") : null;
                String typ = md != null ? md.get("type") : null; // "customer" | "buffet"
                System.out.println("[WEBHOOK] No order found by PI. Trying metadata: orderId=" + orderId + ", type=" + typ);

                if (orderId != null && typ != null) {
                    UUID oid = UUID.fromString(orderId);
                    if ("customer".equalsIgnoreCase(typ)) {
                        customerOrderRepo.findById(oid).ifPresent(o -> {
                            System.out.println("[WEBHOOK] Fallback matched CUSTOMER order " + o.getId());
                            if (o.getPaymentIntentId() == null || o.getPaymentIntentId().isBlank()) {
                                o.setPaymentIntentId(paymentIntentId); // backfill
                            }
                            o.setPaymentStatus(status);
                            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
                            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                                o.setStatus(OrderStatus.CONFIRMED);
                            }
                            customerOrderRepo.save(o);

                            if (status == PaymentStatus.SUCCEEDED) {
                                try {
                                    webPushService.broadcast("admin",
                                            """
                                            {"title":"New Order (Menu)","body":"Paid %s order %s","url":"/admin/orders"}
                                            """.formatted(o.getOrderType(), o.getId()));
                                } catch (Exception ignored) {}
                                try {
                                    customerOrderService.sendCustomerConfirmationWithTrackLink(o);
                                } catch (Exception ignored) {}
                            }
                        });
                    } else if ("buffet".equalsIgnoreCase(typ)) {
                        buffetOrderRepo.findById(oid).ifPresent(o -> {
                            System.out.println("[WEBHOOK] Fallback matched BUFFET order " + o.getId());
                            if (o.getPaymentIntentId() == null || o.getPaymentIntentId().isBlank()) {
                                o.setPaymentIntentId(paymentIntentId); // backfill
                            }
                            o.setPaymentStatus(status);
                            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
                            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                                o.setStatus(OrderStatus.CONFIRMED);
                            }
                            buffetOrderRepo.save(o);

                            if (status == PaymentStatus.SUCCEEDED) {
                                try {
                                    webPushService.broadcast("admin",
                                            """
                                            {"title":"New Order (Buffet)","body":"Paid %s order %s","url":"/admin/buffet-orders"}
                                            """.formatted(o.getOrderType(), o.getId()));
                                } catch (Exception ignored) {}
                                try {
                                    buffetOrderService.sendCustomerConfirmationWithTrackLink(o);
                                } catch (Exception ignored) {}
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("[WEBHOOK] Metadata fallback failed: " + e.getMessage());
            }
        }

        if (!touched) {
            System.out.println("[WEBHOOK] No order updated for PI=" + paymentIntentId);
        }
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
                .orElse(null);
        deliveryZones.assertDeliverableOrThrow(orderType, plz);
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
