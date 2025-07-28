package ch.asiankitchen.controller;

import ch.asiankitchen.model.CustomerOrder;
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
    public CustomerOrder createOrder(@RequestBody CustomerOrder customerOrder) {
        customerOrder.setCreatedAt(LocalDateTime.now());
        customerOrder.setStatus(Status.ORDER_NEW);
        customerOrder.getOrderItems().forEach(item -> item.setCustomerOrder(customerOrder));
        return orderRepository.save(customerOrder);
    }

    @GetMapping("/my-orders")
    public List<CustomerOrder> getMyOrders(@RequestParam UUID userId) {
        return orderRepository.findByUserId(userId);
    }
}
