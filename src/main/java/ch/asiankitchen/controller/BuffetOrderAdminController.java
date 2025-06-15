package ch.asiankitchen.controller;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/buffet-orders")
@CrossOrigin(origins = "http://localhost:3000")
public class BuffetOrderAdminController {

    private final BuffetOrderRepository repo;

    public BuffetOrderAdminController(BuffetOrderRepository r){
        this.repo=r;
    }

    @GetMapping
    public List<BuffetOrder> all(){
        return repo.findAll();
    }

    @PutMapping("/{id}/status")
    public BuffetOrder updateStatus(@PathVariable UUID id, @RequestBody Status s){
        BuffetOrder b=repo.findById(id).orElseThrow();
        b.setStatus(s);
        return repo.save(b);
    }
}
