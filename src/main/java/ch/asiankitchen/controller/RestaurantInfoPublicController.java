package ch.asiankitchen.controller;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.service.RestaurantInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-info")
public class RestaurantInfoPublicController {
    private final RestaurantInfoService service;

    public RestaurantInfoPublicController(RestaurantInfoService service) {
        this.service = service;
    }

    @GetMapping("/current")
    public ResponseEntity<RestaurantInfoReadDTO> getCurrent() {
        return service.getCurrent()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}