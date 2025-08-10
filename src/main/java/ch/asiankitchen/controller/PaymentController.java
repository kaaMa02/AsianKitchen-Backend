package ch.asiankitchen.controller;

import ch.asiankitchen.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/customer-orders/{id}/intent")
    public Map<String, String> createCustomerOrderIntent(@PathVariable UUID id) throws StripeException {
        PaymentIntent pi = service.createIntentForCustomerOrder(id);
        return Map.of("clientSecret", pi.getClientSecret());
    }

    @PostMapping("/buffet-orders/{id}/intent")
    public Map<String, String> createBuffetOrderIntent(@PathVariable UUID id) throws StripeException {
        PaymentIntent pi = service.createIntentForBuffetOrder(id);
        return Map.of("clientSecret", pi.getClientSecret());
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sig) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sig, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                var pi = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    String orderId = pi.getMetadata().get("orderId"); // if you set it
                    // TODO: mark order CONFIRMED, send email, etc.
                }
            }
            case "payment_intent.payment_failed" -> {
                // optional: mark FAILED / notify
            }
        }
        return ResponseEntity.ok("ok");
    }
}