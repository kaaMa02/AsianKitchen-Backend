package ch.asiankitchen.controller;

import ch.asiankitchen.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/public/discounts")
@RequiredArgsConstructor
public class DiscountPublicController {

    private final DiscountService discountService;

    /** Public endpoint used by the storefront to show the current sale. */
    @GetMapping("/active")
    public Map<String, BigDecimal> getActive() {
        var a = discountService.resolveActive();
        BigDecimal menu   = a.percentMenu()   == null ? BigDecimal.ZERO : a.percentMenu();
        BigDecimal buffet = a.percentBuffet() == null ? BigDecimal.ZERO : a.percentBuffet();
        return Map.of(
                "percentMenu", menu,
                "percentBuffet", buffet
        );
    }
}
