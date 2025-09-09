package ch.asiankitchen.controller;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.dto.ReservationWriteDTO;
import ch.asiankitchen.dto.ReservationStatusDTO;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationReadDTO> create(@Valid @RequestBody ReservationWriteDTO dto) {
        ReservationReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Track for guest
    @GetMapping("/{id}/track")
    public ReservationReadDTO track(@PathVariable UUID id, @RequestParam String email) {
        return service.track(id, email);
    }

    // Get one (useful for admin UI)
    @GetMapping("/{id}")
    public ReservationReadDTO getOne(@PathVariable UUID id) {
        return service.getById(id);
    }

    // List by user (you already had this)
    @GetMapping
    public List<ReservationReadDTO> listByUser(@RequestParam UUID userId) {
        return service.listByUser(userId);
    }

    // Admin lists (wire auth later)
    @GetMapping("/pending")
    public List<ReservationReadDTO> listPending() {
        return service.listByStatus(ReservationStatus.REQUESTED);
    }

    @GetMapping("/all")
    public List<ReservationReadDTO> listAll() {
        return service.listAll();
    }

    // Approve / Reject / Cancel by setting status via DTO: { "status": "CONFIRMED" }
    @PatchMapping("/{id}/status")
    public ReservationReadDTO updateStatus(@PathVariable UUID id,
                                           @Valid @RequestBody ReservationStatusDTO body) {
        ReservationStatus status = ReservationStatus.valueOf(body.getStatus().toUpperCase());
        return service.updateStatus(id, status);
    }

}
