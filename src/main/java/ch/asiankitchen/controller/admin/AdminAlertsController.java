package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.AdminAlertsDTO;
import ch.asiankitchen.service.AdminAlertsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/alerts")
public class AdminAlertsController {
    private final AdminAlertsService service;
    public AdminAlertsController(AdminAlertsService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlertsDTO> get() {
        return ResponseEntity.ok(service.summary());
    }
}
