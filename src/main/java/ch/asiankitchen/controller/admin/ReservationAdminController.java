package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
public class ReservationAdminController {
    private final ReservationService service;

    public ReservationAdminController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservationReadDTO> listAll() {
        return service.listAll();
    }

    @PatchMapping("/{id}/status")
    public ReservationReadDTO updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String,String> body) {
        ReservationStatus status = ReservationStatus.valueOf(body.get("status"));
        return service.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}