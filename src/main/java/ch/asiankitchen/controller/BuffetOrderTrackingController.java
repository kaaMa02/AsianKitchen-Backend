package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.service.BuffetOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/buffet-orders")
public class BuffetOrderTrackingController {
    private final BuffetOrderService service;

    public BuffetOrderTrackingController(BuffetOrderService service) {
        this.service = service;
    }

    @GetMapping("/track")
    public BuffetOrderReadDTO track(
            @RequestParam UUID orderId,
            @RequestParam String email) {
        return service.track(orderId, email);
    }
}