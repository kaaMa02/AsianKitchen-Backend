package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.dto.BuffetOrderWriteDTO;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.BuffetOrderRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/buffet-orders")
public class BuffetOrderPublicController {

    private final BuffetOrderRepository buffetOrderRepository;

    public BuffetOrderPublicController(BuffetOrderRepository buffetOrderRepository) {
        this.buffetOrderRepository = buffetOrderRepository;
    }

    @PostMapping
    public BuffetOrderReadDTO createOrder(@Valid @RequestBody BuffetOrderWriteDTO dto) {
        BuffetOrder order = dto.toEntity();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Status.ORDER_NEW);
        order.getBuffetOrderItems().forEach(i -> i.setBuffetOrder(order));
        BuffetOrder saved = buffetOrderRepository.save(order);
        return BuffetOrderReadDTO.fromEntity(saved);
    }

    @GetMapping("/by-user")
    public List<BuffetOrderReadDTO> getOrdersByUser(@RequestParam UUID userId) {
        return buffetOrderRepository.findByUserId(userId).stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .toList();
    }
}
