package ch.asiankitchen.controller;

import ch.asiankitchen.model.Order;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderPublicController {

    private final OrderRepository orderRepository;

    public OrderPublicController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Status.ORDER_NEW);
        order.getOrderItems().forEach(item -> item.setOrder(order));
        return orderRepository.save(order);
    }

    @GetMapping("/my-orders")
    public List<Order> getMyOrders(@RequestParam UUID userId) {
        return orderRepository.findByUserId(userId);
    }
}
