package ch.asiankitchen.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {
    @Value("${stripe.webhook-secret}")
    String webhookSecret;

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
