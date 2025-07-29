package ch.asiankitchen.controller;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.service.CustomerOrderService;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderTrackingController {

    private final CustomerOrderService customerOrderService;

    public OrderTrackingController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/track")
    public CustomerOrderReadDTO track(@RequestParam UUID orderId, @RequestParam String email) {
        return customerOrderService.trackOrder(orderId, email);
    }
}
