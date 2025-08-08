package ch.asiankitchen.controller;

import ch.asiankitchen.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader("Stripe-Signature") String sig) {
        try {
            service.handleWebhook(payload, sig, webhookSecret);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}