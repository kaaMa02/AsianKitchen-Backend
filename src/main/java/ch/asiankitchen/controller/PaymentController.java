package ch.asiankitchen.controller;

import ch.asiankitchen.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        var pi = service.createIntentForCustomerOrder(id);
        return Map.of("clientSecret", pi.getClientSecret());
    }

    @PostMapping("/buffet-orders/{id}/intent")
    public Map<String, String> createBuffetOrderIntent(@PathVariable UUID id) throws StripeException {
        var pi = service.createIntentForBuffetOrder(id);
        return Map.of("clientSecret", pi.getClientSecret());
    }

    /** Stripe webhook (public) */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        try {
            service.handleWebhook(payload, signature, webhookSecret);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unhandled");
        }
    }

    /** Clean 400 for things like out-of-zone delivery address, etc. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}