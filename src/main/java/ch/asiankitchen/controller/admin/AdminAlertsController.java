package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.AdminAlertsDTO;
import ch.asiankitchen.service.AdminAlertsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAlertsController {

    private final AdminAlertsService service;

    public AdminAlertsController(AdminAlertsService service) {
        this.service = service;
    }

    @GetMapping
    public AdminAlertsDTO summary(Authentication auth) {
        return service.unseenForUser(auth.getName());
    }

    public record SeenBody(List<String> kinds) {}

    @PostMapping("/seen")
    public void markSeen(@RequestBody SeenBody body, Authentication auth) {
        service.markSeen(auth.getName(), Set.copyOf(body.kinds() == null ? List.of() : body.kinds()));
    }
}
