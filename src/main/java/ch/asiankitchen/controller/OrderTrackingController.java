package ch.asiankitchen.controller;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.service.CustomerOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderTrackingController {
    private final CustomerOrderService service;

    public OrderTrackingController(CustomerOrderService service) {
        this.service = service;
    }

    @GetMapping("/{id}/track")
    public CustomerOrderReadDTO track(@PathVariable UUID id, @RequestParam String email) {
        return service.track(id, email);
    }
}
