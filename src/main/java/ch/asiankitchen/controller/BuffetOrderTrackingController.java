package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/buffet-orders")
public class BuffetOrderTrackingController {

    private final BuffetOrderRepository buffetOrderRepo;

    public BuffetOrderTrackingController(BuffetOrderRepository buffetOrderRepo) {
        this.buffetOrderRepo = buffetOrderRepo;
    }

    @GetMapping("/track")
    public BuffetOrderReadDTO track(@RequestParam UUID orderId, @RequestParam String email) {
        BuffetOrder order = buffetOrderRepo.findById(orderId)
                .filter(o -> o.getCustomerInfo() != null &&
                        o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new RuntimeException("No matching order found"));

        return BuffetOrderReadDTO.fromEntity(order);
    }
}
