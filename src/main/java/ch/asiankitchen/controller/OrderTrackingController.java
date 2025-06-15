package ch.asiankitchen.controller;

import ch.asiankitchen.model.Order;
import ch.asiankitchen.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderTrackingController {

    private final OrderRepository repo;

    public OrderTrackingController(OrderRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/track")
    public Order track(@RequestParam UUID orderId, @RequestParam String email) {
        return repo.findById(orderId)
                .filter(o->o.getCustomerInfo()!=null
                        && o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .orElseThrow();
    }
}
