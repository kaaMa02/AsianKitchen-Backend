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

        // Enforce delivery area if needed (no separate service required)
        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // 1) Items subtotal (server-authoritative)
        BigDecimal items = order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal price = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2) VAT (2.6%) on items
        BigDecimal vat = calcVat(order.getOrderType(), items);

        // 3) Delivery fee (based on items subtotal)
        BigDecimal deliveryFee = calcDeliveryFee(order.getOrderType(), items);

        // 4) Grand total: items + VAT + delivery
        BigDecimal grand = items.add(vat).add(deliveryFee);

        order.setTotalPrice(grand);
        customerOrderRepo.save(order);

        long amountMinor = toMinor(grand);
        enforceStripeMinimum(amountMinor);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .putMetadata("type", "customer")
                .putMetadata("orderId", order.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

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

        enforceDeliveryZone(order.getOrderType(), order.getCustomerInfo());

        // If your BuffetOrder keeps items similarly to CustomerOrder, recompute like above.
        // Otherwise assume order.getTotalPrice() currently represents *items subtotal*.
        BigDecimal items = Optional.ofNullable(order.getTotalPrice()).orElse(BigDecimal.ZERO);

        BigDecimal vat = calcVat(order.getOrderType(), items);
        BigDecimal deliveryFee = calcDeliveryFee(order.getOrderType(), items);
        BigDecimal grand = items.add(vat).add(deliveryFee);

        order.setTotalPrice(grand);
        buffetOrderRepo.save(order);

        long amountMinor = toMinor(grand);
        enforceStripeMinimum(amountMinor);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .putMetadata("type", "buffet")
                .putMetadata("orderId", order.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

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
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.SUCCEEDED);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.FAILED);
            }
            case "charge.succeeded" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null && ch.getPaymentIntent() != null) {
                    updateByPaymentIntent(ch.getPaymentIntent(), PaymentStatus.SUCCEEDED);
                }
            }
            case "charge.failed" -> {
                Charge ch = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
                if (ch != null && ch.getPaymentIntent() != null) {
                    updateByPaymentIntent(ch.getPaymentIntent(), PaymentStatus.FAILED);
                }
            }
            default -> { /* ignore others */ }
        }
    }

    private void updateByPaymentIntent(String paymentIntentId, PaymentStatus status) {
        customerOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            customerOrderRepo.save(o);
        });

        buffetOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            if (status == PaymentStatus.SUCCEEDED && o.getStatus() == OrderStatus.NEW) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
            buffetOrderRepo.save(o);
        });
    }

    // ───────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ───────────────────────────────────────────────────────────────────────────
    private void enforceDeliveryZone(OrderType orderType, CustomerInfo info) {
        if (orderType != OrderType.DELIVERY) return;

        String plz = Optional.ofNullable(info)
                .map(CustomerInfo::getAddress)
                .map(Address::getPlz)
                .map(String::trim)
                .orElse("");

        if (!allowedPlz.contains(plz)) {
            // Let the controller turn this into a 400
            throw new IllegalArgumentException(rejectMessage);
        }
    }

    private BigDecimal calcVat(OrderType orderType, BigDecimal itemsSubtotal) {
        // Owner wants VAT for TAKEAWAY + DELIVERY
        boolean taxable = (orderType == OrderType.TAKEAWAY || orderType == OrderType.DELIVERY);
        if (!taxable) return BigDecimal.ZERO;

        BigDecimal rate = vatRatePercent
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP); // e.g. 0.026000
        return itemsSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDeliveryFee(OrderType orderType, BigDecimal itemsSubtotal) {
        if (orderType != OrderType.DELIVERY) return BigDecimal.ZERO;

        // > CHF 25 and < CHF 50 => CHF 3
        // > CHF 50              => CHF 5
        if (itemsSubtotal.compareTo(new BigDecimal("25.00")) > 0
                && itemsSubtotal.compareTo(new BigDecimal("50.00")) < 0) {
            return new BigDecimal("3.00");
        } else if (itemsSubtotal.compareTo(new BigDecimal("50.00")) > 0) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO;
    }

    private long toMinor(BigDecimal amountChf) {
        return amountChf.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void enforceStripeMinimum(long amountMinor) {
        // Stripe min for CHF is 50 (0.50 CHF)
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }
    }
}