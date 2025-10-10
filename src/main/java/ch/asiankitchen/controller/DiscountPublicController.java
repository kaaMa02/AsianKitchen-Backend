package ch.asiankitchen.controller;

import ch.asiankitchen.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Public read-only endpoint for showing the currently active discount
 * to the storefront (no auth required).
 */
@RestController
@RequestMapping(path = "/api/public/discounts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DiscountPublicController {

    private final DiscountService discountService;

    @GetMapping("/active")
    public ResponseEntity<ActiveDiscountDTO> getActive() {
        var active = discountService.resolveActive();
        BigDecimal menu   = active.percentMenu()   == null ? BigDecimal.ZERO : active.percentMenu();
        BigDecimal buffet = active.percentBuffet() == null ? BigDecimal.ZERO : active.percentBuffet();

        // Public discounts can change at any time; avoid client/proxy caching.
        return ResponseEntity
                .ok()
                .header("Cache-Control", "no-store")
                .body(new ActiveDiscountDTO(menu, buffet));
    }

    /**
     * Small, explicit DTO instead of a Map for a stable schema and better OpenAPI.
     */
    public record ActiveDiscountDTO(
            BigDecimal percentMenu,
            BigDecimal percentBuffet
    ) {}
}
