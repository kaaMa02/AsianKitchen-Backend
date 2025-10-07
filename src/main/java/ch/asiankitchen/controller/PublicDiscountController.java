package ch.asiankitchen.controller;

import ch.asiankitchen.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/public/discounts")
@RequiredArgsConstructor
public class PublicDiscountController {

    private final DiscountService service;

    @GetMapping("/active")
    public Map<String, BigDecimal> getActive() {
        var a = service.resolveActive();
        return Map.of(
                "percentMenu",   a.percentMenu(),
                "percentBuffet", a.percentBuffet()
        );
    }
}
