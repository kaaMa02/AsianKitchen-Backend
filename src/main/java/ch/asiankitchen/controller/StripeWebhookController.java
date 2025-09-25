package ch.asiankitchen.controller;

import ch.asiankitchen.service.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final StripeWebhookService service;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    public StripeWebhookController(StripeWebhookService service) {
        this.service = service;
    }

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) throws IOException {
        // Read raw payload so Stripe signature check works
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line; while ((line = reader.readLine()) != null) sb.append(line);
        }
        String payload = sb.toString();
        String sigHeader = request.getHeader("Stripe-Signature");

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            service.handle(event);              // delegate
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }
    }
}
