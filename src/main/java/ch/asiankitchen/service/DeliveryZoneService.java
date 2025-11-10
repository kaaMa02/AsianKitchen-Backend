package ch.asiankitchen.service;

import ch.asiankitchen.dto.DeliveryEligibilityDTO;
import ch.asiankitchen.model.OrderType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DeliveryZoneService {

    @Value("${app.delivery.rules:}")
    private String rulesCsv;

    // Back-compat: used only if rulesCsv is blank
    @Value("${app.delivery.allowed-plz:}")
    private String allowedPlzCsv;

    @Value("${app.delivery.reject-message:We don’t deliver to this address.}")
    private String rejectMessage;

    @Value("${app.delivery.min-order-chf:30.00}")
    private BigDecimal minOrderChf;

    @Value("${app.delivery.fee-chf:5.00}")
    private BigDecimal feeChf;

    @Value("${app.delivery.free-threshold-chf:100.00}")
    private BigDecimal freeThresholdChf;

    private List<Rule> rules;

    @PostConstruct
    void init() {
        String source = (rulesCsv != null && !rulesCsv.isBlank())
                ? rulesCsv
                : allowedPlzCsv; // fallback to old exact list

        if (source == null || source.isBlank()) {
            rules = List.of(); // no delivery zone configured -> treat as none
            return;
        }

        List<Rule> parsed = new ArrayList<>();
        for (String raw : source.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) continue;

            // 4600-4615 (range)
            if (token.matches("^\\d{4}-\\d{4}$")) {
                String[] ab = token.split("-");
                int a = Integer.parseInt(ab[0]);
                int b = Integer.parseInt(ab[1]);
                parsed.add(new RangeRule(Math.min(a, b), Math.max(a, b)));
                continue;
            }

            // 46* (prefix)
            if (token.matches("^\\d{1,4}\\*$")) {
                String prefix = token.substring(0, token.length() - 1);
                parsed.add(new PrefixRule(prefix));
                continue;
            }

            // exact 4632
            if (token.matches("^\\d{4}$")) {
                parsed.add(new ExactRule(Integer.parseInt(token)));
                continue;
            }

            // last resort: interpret as regex (advanced admins)
            try {
                Pattern p = Pattern.compile(token);
                parsed.add(new RegexRule(p));
            } catch (Exception ignored) { /* skip bad token */ }
        }
        rules = List.copyOf(parsed);
    }

    public boolean isDeliverablePlz(String plz) {
        if (rules.isEmpty()) return false;
        String n = (plz == null ? "" : plz.trim());
        if (!n.matches("^\\d{4}$")) return false;
        int v = Integer.parseInt(n);
        for (Rule r : rules) {
            if (r.matches(n, v)) return true;
        }
        return false;
    }

    public void assertDeliverableOrThrow(OrderType type, String plz) {
        if (type != OrderType.DELIVERY) return;
        if (!isDeliverablePlz(plz)) {
            throw new IllegalArgumentException(rejectMessage);
        }
    }

    public DeliveryEligibilityDTO eligibilityFor(OrderType type, String plz) {
        boolean ok = (type != OrderType.DELIVERY) || isDeliverablePlz(plz);
        return DeliveryEligibilityDTO.builder()
                .deliverable(ok)
                .message(ok ? null : rejectMessage)
                .minOrderChf(minOrderChf)
                .feeChf(feeChf)
                .freeThresholdChf(freeThresholdChf)
                .build();
    }

    /* ---------------- rules ---------------- */

    private interface Rule {
        boolean matches(String plzStr, int plzInt);
    }

    private static class ExactRule implements Rule {
        final int value;
        ExactRule(int v) { this.value = v; }
        public boolean matches(String _s, int v) { return v == value; }
    }

    private static class PrefixRule implements Rule {
        final String prefix;
        PrefixRule(String p) { this.prefix = p; }
        public boolean matches(String s, int _v) { return s.startsWith(prefix); }
    }

    private static class RangeRule implements Rule {
        final int a, b;
        RangeRule(int a, int b) { this.a = a; this.b = b; }
        public boolean matches(String _s, int v) { return v >= a && v <= b; }
    }

    private static class RegexRule implements Rule {
        final Pattern p;
        RegexRule(Pattern p) { this.p = p; }
        public boolean matches(String s, int _v) { return p.matcher(s).matches(); }
    }
}
