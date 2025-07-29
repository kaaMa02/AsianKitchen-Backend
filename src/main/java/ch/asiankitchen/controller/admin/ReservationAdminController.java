package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/reservations")
public class ReservationAdminController {

    private final ReservationRepository repo;

    public ReservationAdminController(ReservationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ReservationReadDTO> all() {
        return repo.findAll()
                .stream()
                .map(ReservationReadDTO::fromEntity)
                .toList();
    }

    @PutMapping("/{id}/status")
    public ReservationReadDTO updateStatus(@PathVariable UUID id, @RequestBody Status status) {
        Reservation r = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        r.setStatus(status);
        return ReservationReadDTO.fromEntity(repo.save(r));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        repo.deleteById(id);
    }
}
