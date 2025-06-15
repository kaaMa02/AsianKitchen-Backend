package ch.asiankitchen.controller;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/reservations")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationAdminController {

    private final ReservationRepository repo;

    public ReservationAdminController(ReservationRepository r){
        this.repo=r;
    }

    @GetMapping
    public List<Reservation> all(){
        return repo.findAll();
    }

    @PutMapping("/{id}/status")
    public Reservation updateStatus(@PathVariable UUID id,@RequestBody Status s){
        Reservation r=repo.findById(id)
                .orElseThrow();
        r.setStatus(s);
        return repo.save(r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        repo.deleteById(id);
    }
}
