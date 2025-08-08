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
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        long amountMinor = order.getTotalPrice()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountMinor);
        params.put("currency", currency);
        params.put("metadata", Map.of("type", "customer", "orderId", order.getId().toString()));

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

        long amountMinor = order.getTotalPrice()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountMinor);
        params.put("currency", currency);
        params.put("metadata", Map.of("type", "buffet", "orderId", order.getId().toString()));

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
