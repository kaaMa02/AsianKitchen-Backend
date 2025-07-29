package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/buffet-orders")
public class BuffetOrderAdminController {

    private final BuffetOrderRepository buffetOrderRepository;

    public BuffetOrderAdminController(BuffetOrderRepository buffetOrderRepository) {
        this.buffetOrderRepository = buffetOrderRepository;
    }

    @GetMapping
    public List<BuffetOrderReadDTO> getAllOrders(){
        return buffetOrderRepository.findAll().stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .toList();
    }

    @PutMapping("/{id}/status")
    public BuffetOrderReadDTO updateStatus(@PathVariable UUID id, @RequestBody Status newStatus) {
        BuffetOrder order = buffetOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuffetOrder not found"));
        order.setStatus(newStatus);
        BuffetOrder saved = buffetOrderRepository.save(order);
        return BuffetOrderReadDTO.fromEntity(saved);
    }
}
