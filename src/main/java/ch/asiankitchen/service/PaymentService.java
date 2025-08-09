package ch.asiankitchen.service;

import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
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

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }

    @Transactional
    public PaymentIntent createIntentForCustomerOrder(UUID id) throws StripeException {
        // 1) Load with items & prices
        CustomerOrder order = customerOrderRepo.findWithItemsAndPrices(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        // 2) Recompute total from DB
        java.math.BigDecimal total = order.getOrderItems().stream()
                .map(oi -> {
                    // guard against null price
                    java.math.BigDecimal price = oi.getMenuItem().getPrice();
                    if (price == null) price = java.math.BigDecimal.ZERO;
                    return price.multiply(java.math.BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Persist the (re)calculated total (optional but useful)
        order.setTotalPrice(total);
        customerOrderRepo.save(order);

        long amountMinor = total.movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();

        // 3) Stripe minimum for CHF is 50 (CHF 0.50)
        final long MIN_CHF = 50L;
        if ("chf".equalsIgnoreCase(currency) && amountMinor < MIN_CHF) {
            throw new IllegalArgumentException(
                    "Order total is below Stripe minimum charge for CHF (0.50). " +
                            "Add more items before paying."
            );
        }

        // 4) Create the PaymentIntent with Automatic Payment Methods
        com.stripe.param.PaymentIntentCreateParams params =
                com.stripe.param.PaymentIntentCreateParams.builder()
                        .setAmount(amountMinor)
                        .setCurrency(currency) // keep lower-case, e.g. "chf"
                        .setAutomaticPaymentMethods(
                                com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder().setEnabled(true).build()
                        )
                        .putMetadata("type", "customer")
                        .putMetadata("orderId", order.getId().toString())
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        order.setPaymentIntentId(intent.getId());
        order.setPaymentStatus(ch.asiankitchen.model.PaymentStatus.REQUIRES_PAYMENT_METHOD);
        customerOrderRepo.save(order);
        return intent;
    }

    @Transactional
    public PaymentIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        long amountMinor = order.getTotalPrice()
                .movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();

        final long MIN_CHF = 50L;
        if ("chf".equalsIgnoreCase(currency) && amountMinor < MIN_CHF) {
            throw new IllegalArgumentException(
                    "Order total is below Stripe minimum charge for CHF (0.50)."
            );
        }

        com.stripe.param.PaymentIntentCreateParams params =
                com.stripe.param.PaymentIntentCreateParams.builder()
                        .setAmount(amountMinor)
                        .setCurrency(currency)
                        .setAutomaticPaymentMethods(
                                com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder().setEnabled(true).build()
                        )
                        .putMetadata("type", "buffet")
                        .putMetadata("orderId", order.getId().toString())
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        order.setPaymentIntentId(intent.getId());
        order.setPaymentStatus(ch.asiankitchen.model.PaymentStatus.REQUIRES_PAYMENT_METHOD);
        buffetOrderRepo.save(order);
        return intent;
    }

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
            default -> {
                // ignore others for now
            }
        }
    }

    private void updateByPaymentIntent(String paymentIntentId, PaymentStatus status) {
        Optional<CustomerOrder> co = customerOrderRepo.findByPaymentIntentId(paymentIntentId);
        co.ifPresent(o -> {
            o.setPaymentStatus(status);
            customerOrderRepo.save(o);
        });

        Optional<BuffetOrder> bo = buffetOrderRepo.findByPaymentIntentId(paymentIntentId);
        bo.ifPresent(o -> {
            o.setPaymentStatus(status);
            buffetOrderRepo.save(o);
        });
    }
}
