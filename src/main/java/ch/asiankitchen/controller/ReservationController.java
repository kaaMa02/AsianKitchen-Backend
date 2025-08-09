package ch.asiankitchen.controller;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.dto.ReservationWriteDTO;
import ch.asiankitchen.dto.ReservationStatusDTO;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ReservationReadDTO> create(
            @Valid @RequestBody ReservationWriteDTO dto) {
        ReservationReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}/track")
    public ReservationReadDTO track(@PathVariable UUID id, @RequestParam String email) {
        return service.track(id, email);
    }

    @GetMapping
    public List<ReservationReadDTO> listByUser(@RequestParam UUID userId) {
        return service.listByUser(userId);
    }

}
