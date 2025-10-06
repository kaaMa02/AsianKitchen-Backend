package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.service.CustomerOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerOrderAdminController {
    private final CustomerOrderService service;

    public CustomerOrderAdminController(CustomerOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerOrderReadDTO> listAll() {
        return service.listAllVisibleForAdmin();
    }

    @PatchMapping("/{id}/status")
    public CustomerOrderReadDTO updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String,String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return service.updateStatus(id, status);
    }
}