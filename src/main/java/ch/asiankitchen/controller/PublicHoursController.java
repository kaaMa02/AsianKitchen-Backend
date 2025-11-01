package ch.asiankitchen.controller;

import ch.asiankitchen.dto.HoursStatusDTO;
import ch.asiankitchen.service.HoursService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/public/hours")
public class PublicHoursController {

    private final HoursService hours;

    public PublicHoursController(HoursService hours) {
        this.hours = hours;
    }

    /** GET /api/public/hours/status?orderType=DELIVERY|TAKEAWAY&at=2025-11-01T17:30:00Z */
    @GetMapping("/status")
    public HoursStatusDTO status(@RequestParam(defaultValue = "TAKEAWAY") String orderType,
                                 @RequestParam(required = false) String at) {
        boolean forDelivery = "DELIVERY".equalsIgnoreCase(orderType);
        if (at == null || at.isBlank()) {
            return hours.statusNow(forDelivery);
        }
        Instant ts = Instant.parse(at);
        return hours.statusAt(ts, forDelivery);
    }
}
