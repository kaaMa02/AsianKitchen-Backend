package ch.asiankitchen.controller;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/buffet-orders")
@CrossOrigin(origins = "http://localhost:3000")
public class BuffetOrderPublicController {

    private final BuffetOrderRepository repo;

    public BuffetOrderPublicController(BuffetOrderRepository r){
        this.repo=r;
    }

    @PostMapping
    public BuffetOrder create(@RequestBody BuffetOrder b) {
        b.setCreatedAt(LocalDateTime.now());
        b.setStatus(Status.ORDER_NEW);
        b.getBuffetOrderItems().forEach(i->i.setBuffetOrder(b));
        return repo.save(b);
    }

    @GetMapping("/my-orders")
    public List<BuffetOrder> mine(@RequestParam UUID userId){
        return repo.findByUserId(userId);
    }
}
