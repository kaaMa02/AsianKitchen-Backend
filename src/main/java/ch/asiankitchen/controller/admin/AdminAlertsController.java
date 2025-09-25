package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.AdminAlertsDTO;
import ch.asiankitchen.service.AdminAlertsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAlertsController {
    private final AdminAlertsService service;

    public AdminAlertsController(AdminAlertsService service) { this.service = service; }

    @GetMapping
    public AdminAlertsDTO get() {
        return service.summary();
    }

    // Frontend uses this to clear local badges. Server counts are DB-derived, so this is a no-op.
    @PostMapping("/seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markSeen(@RequestBody(required = false) Map<String, List<String>> ignored) { }
}
