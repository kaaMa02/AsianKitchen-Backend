package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.service.CustomerOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
public class CustomerOrderAdminController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderAdminController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping
    public List<CustomerOrderReadDTO> getAllOrders() {
        return customerOrderService.findAll();
    }

    @PutMapping("/{id}/status")
    public CustomerOrderReadDTO updateOrderStatus(@PathVariable UUID id, @RequestBody Status status) {
        return customerOrderService.updateStatus(id, status);
    }
}
