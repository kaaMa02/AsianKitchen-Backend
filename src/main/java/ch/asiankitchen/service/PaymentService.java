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
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
        // Load the order (make sure it has items+prices in your query/mapping)
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        // Recompute total on server
        BigDecimal total = order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal price = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        customerOrderRepo.save(order);

        long amountMinor = total.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        // Stripe min for CHF is 50 (0.50 CHF)
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                )
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

    @Transactional
    public PaymentIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        long amountMinor = order.getTotalPrice().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if ("chf".equalsIgnoreCase(currency) && amountMinor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                )
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

    @Transactional
    public void handleWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.SUCCEEDED);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) updateByPaymentIntent(pi.getId(), PaymentStatus.FAILED);
            }
            default -> { /* ignore */ }
        }
    }

    private void updateByPaymentIntent(String paymentIntentId, PaymentStatus status) {
        customerOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            customerOrderRepo.save(o);
        });
        buffetOrderRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> {
            o.setPaymentStatus(status);
            buffetOrderRepo.save(o);
        });
    }
}