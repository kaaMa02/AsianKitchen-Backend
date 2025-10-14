package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.dto.BuffetOrderWriteDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.service.BuffetOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/buffet-orders")
public class BuffetOrderController {
    private final BuffetOrderService service;

    public BuffetOrderController(BuffetOrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BuffetOrderReadDTO> create(@Valid @RequestBody BuffetOrderWriteDTO dto) {
        BuffetOrderReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public BuffetOrderReadDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping
    public List<BuffetOrderReadDTO> listByUser(@RequestParam UUID userId) {
        return service.listByUser(userId);
    }

    @PatchMapping("/{id}/status")
    public BuffetOrderReadDTO updateStatus(@PathVariable UUID id, @RequestBody Map<String,String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return service.updateStatus(id, status);
    }

    /** Tracking (query style) — public */
    @GetMapping("/track")
    public BuffetOrderReadDTO trackByQuery(
            @RequestParam UUID orderId,
            @RequestParam String email) {
        return service.track(orderId, email);
    }

    /** Tracking (path style) — public, equivalent to the above */
    @GetMapping("/{id}/track")
    public BuffetOrderReadDTO trackByPath(
            @PathVariable("id") UUID id,
            @RequestParam String email) {
        return service.track(id, email);
    }
}