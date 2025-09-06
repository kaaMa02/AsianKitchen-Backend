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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:chf}")
    private String currency;

    /** VAT rate in percent (e.g., 2.6 for 2.60%). Only applied to takeaway/delivery totals. */
    @Value("${vat.ratePercent:2.6}")
    private BigDecimal vatRatePercent;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // CUSTOMER ORDER
    // ───────────────────────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForCustomerOrder(UUID id) throws StripeException {
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        // 1) Subtotal from items
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal price = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2) Delivery fee rule (owners’ rule)
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (order.getOrderType() == OrderType.DELIVERY) {
            // > 50 CHF → 5 CHF ; 25–50 CHF (strictly >25 and <50) → 3 CHF ; ≤25 CHF → 0 CHF
            if (subtotal.compareTo(new BigDecimal("50.00")) > 0) {
                deliveryFee = new BigDecimal("5.00");
            } else if (subtotal.compareTo(new BigDecimal("25.00")) > 0) {
                deliveryFee = new BigDecimal("3.00");
            }
        }

        // 3) VAT (2.6%) on food subtotal only
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.026"))
                .setScale(2, RoundingMode.HALF_UP);

        // 4) Final total charged
        BigDecimal grandTotal = subtotal.add(deliveryFee).add(vat);

        // Persist the subtotal as your order’s totalPrice (no schema change).
        order.setTotalPrice(subtotal);
        order.setPaymentStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        customerOrderRepo.save(order);

        long amountMinor = grandTotal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        // Stripe minimum for CHF
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }

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
                // Optional: surface components for later reporting
                .putMetadata("subtotal_chf", subtotal.toPlainString())
                .putMetadata("delivery_fee_chf", deliveryFee.toPlainString())
                .putMetadata("vat_chf", vat.toPlainString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        order.setPaymentIntentId(intent.getId());
        customerOrderRepo.save(order);

        return intent;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Buffet ORDER
    // ───────────────────────────────────────────────────────────────────────────
    @Transactional
    public PaymentIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        BigDecimal subtotal = Optional.ofNullable(order.getTotalPrice()).orElse(BigDecimal.ZERO);

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (order.getOrderType() == OrderType.DELIVERY) {
            if (subtotal.compareTo(new BigDecimal("50.00")) > 0) {
                deliveryFee = new BigDecimal("5.00");
            } else if (subtotal.compareTo(new BigDecimal("25.00")) > 0) {
                deliveryFee = new BigDecimal("3.00");
            }
        }

        BigDecimal vat = subtotal.multiply(new BigDecimal("0.026"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal.add(deliveryFee).add(vat);

        order.setPaymentStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        buffetOrderRepo.save(order);

        long amountMinor = grandTotal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }

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
                .putMetadata("subtotal_chf", subtotal.toPlainString())
                .putMetadata("delivery_fee_chf", deliveryFee.toPlainString())
                .putMetadata("vat_chf", vat.toPlainString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        order.setPaymentIntentId(intent.getId());
        buffetOrderRepo.save(order);

        return intent;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // WEBHOOK HANDLER
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
    private BigDecimal applyVat(OrderType orderType, BigDecimal subtotal) {
        // Only for TAKEAWAY/DELIVERY — change if you want dine-in too.
        boolean taxable = (orderType == OrderType.TAKEAWAY || orderType == OrderType.DELIVERY);
        if (!taxable) return BigDecimal.ZERO;

        BigDecimal rate = vatRatePercent
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP); // 0.026000 for 2.60%
        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
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