package ch.asiankitchen.controller;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.dto.CustomerOrderWriteDTO;
import ch.asiankitchen.dto.OrderStatusDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    private final CustomerOrderService service;

    public CustomerOrderController(CustomerOrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerOrderReadDTO> create(
            @Valid @RequestBody CustomerOrderWriteDTO dto) {
        CustomerOrderReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public CustomerOrderReadDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CustomerOrderReadDTO> listByUser(@RequestParam UUID userId) {
        return service.listByUser(userId);
    }

    @PatchMapping("/{id}/status")
    public CustomerOrderReadDTO updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusDTO dto) {
        OrderStatus newStatus = OrderStatus.valueOf(dto.getStatus());
        return service.updateStatus(id, newStatus);
    }

    @GetMapping("/track")
    public CustomerOrderReadDTO track(
            @RequestParam UUID orderId,
            @RequestParam String email) {
        return service.track(orderId, email);
    }
}
