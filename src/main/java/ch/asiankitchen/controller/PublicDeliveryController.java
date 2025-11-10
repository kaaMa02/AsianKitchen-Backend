package ch.asiankitchen.controller;

import ch.asiankitchen.dto.DeliveryEligibilityDTO;
import ch.asiankitchen.model.OrderType;
import ch.asiankitchen.service.DeliveryZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/delivery")
@RequiredArgsConstructor
public class PublicDeliveryController {

    private final DeliveryZoneService zones;

    /** Example: GET /api/public/delivery/eligibility?orderType=DELIVERY&plz=4632 */
    @GetMapping("/eligibility")
    public DeliveryEligibilityDTO eligibility(
            @RequestParam(defaultValue = "TAKEAWAY") String orderType,
            @RequestParam(required = false) String plz) {
        OrderType typ = "DELIVERY".equalsIgnoreCase(orderType) ? OrderType.DELIVERY : OrderType.TAKEAWAY;
        return zones.eligibilityFor(typ, plz);
    }
}
