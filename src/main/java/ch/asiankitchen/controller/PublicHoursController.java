package ch.asiankitchen.controller;

import ch.asiankitchen.dto.HoursStatusDTO;
import ch.asiankitchen.service.HoursService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/hours")
public class PublicHoursController {

    private final HoursService hours;

    public PublicHoursController(HoursService hours) {
        this.hours = hours;
    }

    /** Example: GET /api/public/hours/status?orderType=DELIVERY|TAKEAWAY */
    @GetMapping("/status")
    public HoursStatusDTO status(@RequestParam(defaultValue = "TAKEAWAY") String orderType) {
        boolean forDelivery = "DELIVERY".equalsIgnoreCase(orderType);
        return hours.status(forDelivery);
    }
}
