package ch.asiankitchen.controller;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/buffet-orders")
@CrossOrigin(origins = "http://localhost:3000")
public class BuffetOrderTrackingController {

    private final BuffetOrderRepository buffetOrderRepo;

    public BuffetOrderTrackingController(BuffetOrderRepository buffetOrderRepo) {
        this.buffetOrderRepo = buffetOrderRepo;
    }

    @GetMapping("/track")
    public BuffetOrder track(@RequestParam UUID orderId, @RequestParam String email) {
        return buffetOrderRepo.findById(orderId)
                .filter(o->o.getCustomerInfo()!=null
                        && o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .orElseThrow();
    }
}
