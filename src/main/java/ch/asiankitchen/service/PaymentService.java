package ch.asiankitchen.service;

import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Charge;
import com.stripe.model.tax.Calculation;
import com.stripe.model.EventDataObjectDeserializer;

import com.stripe.net.Webhook;

import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.tax.CalculationCreateParams;

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

    /** Small holder so controller can return amounts as well. */
    public static class CreatedIntent {
        private final PaymentIntent intent;
        private final Calculation calc;
        public CreatedIntent(PaymentIntent intent, Calculation calc) {
            this.intent = intent; this.calc = calc;
        }
        public PaymentIntent getIntent() { return intent; }
        public Calculation getCalc() { return calc; }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  CUSTOMER ORDER
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public CreatedIntent createIntentForCustomerOrder(UUID id) throws StripeException {
        CustomerOrder order = customerOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));

        CalculationCreateParams.Builder calcBuilder = baseCalculation(order);

        order.getOrderItems().forEach(oi -> {
            BigDecimal price = Optional.ofNullable(oi.getMenuItem().getPrice()).orElse(BigDecimal.ZERO);
            int qty = Optional.ofNullable(oi.getQuantity()).orElse(0);
            long amountMinor = toMinor(price.multiply(BigDecimal.valueOf(qty)));

            calcBuilder.addLineItem(
                    CalculationCreateParams.LineItem.builder()
                            .setAmount(amountMinor) // VAT-inclusive line amount
                            .setQuantity((long) qty)
                            .setReference(
                                    oi.getMenuItem() != null && oi.getMenuItem().getId() != null
                                            ? oi.getMenuItem().getId().toString()
                                            : "item"
                            )
                            .setTaxBehavior(CalculationCreateParams.LineItem.TaxBehavior.INCLUSIVE)
                            .build()
            );
        });

        Calculation calc = Calculation.create(calcBuilder.build());

        long amountTotal = calc.getAmountTotal(); // always present
        enforceStripeMin(amountTotal);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountTotal)
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

        return new CreatedIntent(intent, calc);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  BUFFET ORDER
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public CreatedIntent createIntentForBuffetOrder(UUID id) throws StripeException {
        BuffetOrder order = buffetOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));

        CalculationCreateParams.Builder calcBuilder = baseCalculation(order);

        long totalMinor = toMinor(order.getTotalPrice());
        calcBuilder.addLineItem(
                CalculationCreateParams.LineItem.builder()
                        .setAmount(totalMinor)
                        .setQuantity(1L)
                        .setReference("buffet_total")
                        .setTaxBehavior(CalculationCreateParams.LineItem.TaxBehavior.INCLUSIVE)
                        .build()
        );

        Calculation calc = Calculation.create(calcBuilder.build());

        long amountTotal = calc.getAmountTotal();
        enforceStripeMin(amountTotal);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountTotal)
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

        return new CreatedIntent(intent, calc);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  WEBHOOKS (unchanged logic)
    // ─────────────────────────────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String signatureHeader, String webhookSecret)
            throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                String piId = extractPaymentIntentId(event);
                if (piId != null) updateByPaymentIntent(piId, PaymentStatus.SUCCEEDED);
            }
            case "payment_intent.payment_failed" -> {
                String piId = extractPaymentIntentId(event);
                if (piId != null) updateByPaymentIntent(piId, PaymentStatus.FAILED);
            }
            case "charge.succeeded" -> {
                String piId = extractPaymentIntentIdFromCharge(event);
                if (piId != null) updateByPaymentIntent(piId, PaymentStatus.SUCCEEDED);
            }
            case "charge.failed" -> {
                String piId = extractPaymentIntentIdFromCharge(event);
                if (piId != null) updateByPaymentIntent(piId, PaymentStatus.FAILED);
            }
            default -> { /* ignore */ }
        }
    }

    private String extractPaymentIntentId(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        if (d.getObject().isPresent() && d.getObject().get() instanceof PaymentIntent pi) {
            return pi.getId();
        }
        String raw = d.getRawJson();
        if (raw != null) {
            JsonObject o = JsonParser.parseString(raw).getAsJsonObject();
            if (o.has("id")) return o.get("id").getAsString();
            if (o.has("payment_intent")) return o.get("payment_intent").getAsString();
        }
        return null;
    }

    private String extractPaymentIntentIdFromCharge(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        if (d.getObject().isPresent() && d.getObject().get() instanceof Charge ch) {
            return ch.getPaymentIntent();
        }
        String raw = d.getRawJson();
        if (raw != null) {
            JsonObject o = JsonParser.parseString(raw).getAsJsonObject();
            if (o.has("payment_intent")) return o.get("payment_intent").getAsString();
        }
        return null;
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

    // ─────────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    /** Sum tax from breakdown (works across SDK versions). */
    public long calcTaxAmount(Calculation calc) {
        if (calc.getTaxBreakdown() == null) return 0L;
        long sum = 0L;
        for (var tb : calc.getTaxBreakdown()) {
            // old/new SDKs expose amount as getAmount()
            Long a = tb.getAmount();
            if (a != null) sum += a;
        }
        return sum;
    }

    /** Try to read the VAT % from rate details; fallback to tax/net. */
    public Double extractVatRatePct(Calculation calc) {
        try {
            if (calc.getTaxBreakdown() != null && !calc.getTaxBreakdown().isEmpty()) {
                var details = calc.getTaxBreakdown().get(0).getTaxRateDetails();
                if (details != null) {
                    // Some SDKs expose percentage as decimal-string
                    String pctStr = null;
                    try {
                        // try percentageDecimal() first (newer)
                        var m = details.getClass().getMethod("getPercentageDecimal");
                        Object v = m.invoke(details);
                        pctStr = (v instanceof String) ? (String) v : null;
                    } catch (NoSuchMethodException ignore) {
                        // fallback: try getPercentage() (older/newer API)
                        try {
                            var m2 = details.getClass().getMethod("getPercentage");
                            Object v2 = m2.invoke(details);
                            if (v2 instanceof Number n) return n.doubleValue();
                        } catch (Exception ignore2) { /* fall through */ }
                    }
                    if (pctStr != null) {
                        return Double.parseDouble(pctStr);
                    }
                }
            }
        } catch (Exception ignore) {}

        long total = calc.getAmountTotal();
        long tax = calcTaxAmount(calc);
        long net = Math.max(0L, total - tax);
        if (net > 0) return (tax * 100.0) / net;
        return null;
    }

    /** Convert CHF to rappen (minor units). */
    private static long toMinor(BigDecimal chf) {
        if (chf == null) return 0L;
        return chf.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void enforceStripeMin(long minor) {
        if ("chf".equalsIgnoreCase(currency) && minor < 50L) {
            throw new IllegalArgumentException("Order total is below Stripe minimum charge for CHF (0.50).");
        }
    }

    /** Build the Calculation base with currency + customer address (CH). */
    private CalculationCreateParams.Builder baseCalculation(CustomerOrder order) {
        var b = CalculationCreateParams.builder().setCurrency(currency.toLowerCase());
        var info = order.getCustomerInfo();
        if (info != null && info.getAddress() != null) {
            var a = info.getAddress();
            b.setCustomerDetails(
                    CalculationCreateParams.CustomerDetails.builder()
                            .setAddress(
                                    CalculationCreateParams.CustomerDetails.Address.builder()
                                            .setCountry("CH")
                                            .setCity(a.getCity())
                                            .setPostalCode(a.getPlz())
                                            .setLine1(
                                                    (a.getStreet() != null ? a.getStreet() : "") +
                                                            (a.getStreetNo() != null ? " " + a.getStreetNo() : "")
                                            )
                                            .build()
                            ).build()
            );
        } else {
            b.setCustomerDetails(
                    CalculationCreateParams.CustomerDetails.builder()
                            .setAddress(CalculationCreateParams.CustomerDetails.Address.builder()
                                    .setCountry("CH").build()
                            ).build()
            );
        }
        return b;
    }

    private CalculationCreateParams.Builder baseCalculation(BuffetOrder order) {
        var b = CalculationCreateParams.builder().setCurrency(currency.toLowerCase());
        var info = order.getCustomerInfo();
        if (info != null && info.getAddress() != null) {
            var a = info.getAddress();
            b.setCustomerDetails(
                    CalculationCreateParams.CustomerDetails.builder()
                            .setAddress(
                                    CalculationCreateParams.CustomerDetails.Address.builder()
                                            .setCountry("CH")
                                            .setCity(a.getCity())
                                            .setPostalCode(a.getPlz())
                                            .setLine1(
                                                    (a.getStreet() != null ? a.getStreet() : "") +
                                                            (a.getStreetNo() != null ? " " + a.getStreetNo() : "")
                                            )
                                            .build()
                            ).build()
            );
        } else {
            b.setCustomerDetails(
                    CalculationCreateParams.CustomerDetails.builder()
                            .setAddress(CalculationCreateParams.CustomerDetails.Address.builder()
                                    .setCountry("CH").build()
                            ).build()
            );
        }
        return b;
    }
}
