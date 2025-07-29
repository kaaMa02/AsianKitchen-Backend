package ch.asiankitchen.controller;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.dto.CustomerOrderWriteDTO;
import ch.asiankitchen.service.CustomerOrderService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderPublicController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderPublicController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping
    public CustomerOrderReadDTO createOrder(@RequestBody CustomerOrderWriteDTO dto) {
        return customerOrderService.createOrder(dto);
    }

    @GetMapping("/my-orders")
    public List<CustomerOrderReadDTO> getMyOrders(@RequestParam UUID userId) {
        return customerOrderService.getOrdersByUserId(userId);
    }
}
