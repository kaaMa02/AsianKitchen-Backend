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

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:chf}")
    private String currency;

    /** VAT rate in percent (e.g. 2.6 for 2.60%). Applies to TAKEAWAY & DELIVERY. */
    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    /** Delivery zone config injected from application properties / env. */
    @Value("${app.delivery.allowed-plz:}")
    private String allowedPlzCsv;

    @Value("${app.delivery.reject-message:We don’t deliver to this address.}")
    private String rejectMessage;

    /** Parsed set of allowed PLZ codes. */
    private Set<String> allowedPlz;

    @Value("${app.delivery.min-order-chf:30.00}")
    private BigDecimal minDeliveryOrder;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal deliveryFee;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeDeliveryThreshold;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
        allowedPlz = Arrays.stream(allowedPlzCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // ───────────────────────────────────────────────────────────────────────────
    // CUSTOMER ORDER
    // ───────────────────────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForCustomerOrder(UUID id) throws StripeException {
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        // Cash orders never need Stripe intents
        if (order.getPaymentMethod() == PaymentMethod.CASH) {
            throw new IllegalArgumentException("Cash orders do not require a payment intent.");
        }

        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // Items subtotal from DB prices (server-authoritative)
        BigDecimal items = order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal price = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        enforceDeliveryMinimum(order.getOrderType(), items);

        BigDecimal grand = computeGrandTotal(order.getOrderType(), items);
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

    // ───────────────────────────────────────────────────────────────────────────
    // BUFFET ORDER
    // ───────────────────────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        // Cash orders never need Stripe intents
        if (order.getPaymentMethod() == PaymentMethod.CASH) {
            throw new IllegalArgumentException("Cash orders do not require a payment intent.");
        }

        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // Buffet order keeps item prices in totalPrice; treat that as items subtotal
        BigDecimal items = Optional.ofNullable(order.getTotalPrice()).orElse(BigDecimal.ZERO);
        enforceDeliveryMinimum(order.getOrderType(), items);
        BigDecimal grand = computeGrandTotal(order.getOrderType(), items);
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

    // ───────────────────────────────────────────────────────────────────────────
    // WEBHOOK HANDLER (status -> orders)
    // ───────────────────────────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.SUCCEEDED, null);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.FAILED, null);
            }
            case "charge.succeeded" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null && ch.getPaymentIntent() != null) {
                    updateByPaymentIntent(ch.getPaymentIntent(),
                            PaymentStatus.SUCCEEDED,
                            resolveMethod(ch));
                }
            }
            case "charge.failed" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null && ch.getPaymentIntent() != null) {
                    updateByPaymentIntent(ch.getPaymentIntent(),
                            PaymentStatus.FAILED,
                            resolveMethod(ch));
                }
            }
            default -> { /* ignore others */ }
        }
    }

    private void enforceDeliveryMinimum(OrderType orderType, BigDecimal itemsSubtotal) {
        if (orderType != OrderType.DELIVERY) return;
        if (itemsSubtotal.compareTo(minDeliveryOrder) < 0) {
            throw new IllegalArgumentException("Minimum delivery order is CHF " + minDeliveryOrder.setScale(2));
        }
    }

    /** Map Stripe charge payment method to our enum. Extend when enabling more methods. */
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

    private void updateByPaymentIntent(String paymentIntentId,
                                       PaymentStatus status,
                                       PaymentMethod maybeMethod) {
        customerOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            customerOrderRepo.save(o);
        });

        buffetOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            if (maybeMethod != null) o.setPaymentMethod(maybeMethod);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            buffetOrderRepo.save(o);
        });
    }

    // ───────────────────────────────────────────────────────────────────────────
    // SMALL HELPERS (eliminate duplication)
    // ───────────────────────────────────────────────────────────────────────────

    /** items subtotal -> grand total (adds VAT + delivery if applicable). */
    private BigDecimal computeGrandTotal(OrderType orderType, BigDecimal itemsSubtotal) {
        BigDecimal vat = calcVat(orderType, itemsSubtotal);
        BigDecimal delivery = calcDeliveryFee(orderType, itemsSubtotal);
        return itemsSubtotal.add(vat).add(delivery);
    }

    /** Build the Stripe PaymentIntent (shared between order types). */
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
                // If you want to hard-restrict to card/TWINT only, uncomment:
                // .addAllPaymentMethodType(Arrays.asList("card", "twint"))
                .putMetadata("type", orderTypeMeta)
                .putMetadata("orderId", orderId.toString())
                .putMetadata("intendedMethod", String.valueOf(intended));

        return PaymentIntent.create(builder.build());
    }

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

    private BigDecimal calcVat(OrderType orderType, BigDecimal itemsSubtotal) {
        boolean taxable = (orderType == OrderType.TAKEAWAY || orderType == OrderType.DELIVERY);
        if (!taxable) return BigDecimal.ZERO;

        BigDecimal rate = vatRatePercent
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return itemsSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDeliveryFee(OrderType orderType, BigDecimal itemsSubtotal) {
        if (orderType != OrderType.DELIVERY) return BigDecimal.ZERO;
        return (itemsSubtotal.compareTo(freeDeliveryThreshold) >= 0) ? BigDecimal.ZERO : deliveryFee;
    }

    private long toMinor(BigDecimal amountChf) {
        return amountChf.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void enforceStripeMinimum(long amountMinor) {
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }
    }
}
