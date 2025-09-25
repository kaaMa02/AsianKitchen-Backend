package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.service.BuffetOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/buffet-orders")
@PreAuthorize("hasRole('ADMIN')")
public class BuffetOrderAdminController {
    private final BuffetOrderService service;

    public BuffetOrderAdminController(BuffetOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<BuffetOrderReadDTO> listAll() {
        return service.listAllPaid();
    }

    @PatchMapping("/{id}/status")
    public BuffetOrderReadDTO updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return service.updateStatus(id, status);
    }
}