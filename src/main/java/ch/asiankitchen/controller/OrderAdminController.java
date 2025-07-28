package ch.asiankitchen.controller;

import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderAdminController {

    private final OrderRepository orderRepository;

    public OrderAdminController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @PutMapping("/{id}/status")
    public CustomerOrder updateOrderStatus(@PathVariable UUID id, @RequestBody Status status) {
        CustomerOrder customerOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerOrder not found"));
        customerOrder.setStatus(status);
        return orderRepository.save(customerOrder);
    }
}
