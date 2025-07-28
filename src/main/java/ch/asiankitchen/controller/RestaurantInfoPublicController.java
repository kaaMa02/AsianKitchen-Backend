package ch.asiankitchen.controller;

import ch.asiankitchen.model.RestaurantInfo;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-info")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantInfoPublicController {

    private final RestaurantInfoRepository restaurantInfoRepository;

    public RestaurantInfoPublicController(RestaurantInfoRepository restaurantInfoRepository) {
        this.restaurantInfoRepository = restaurantInfoRepository;
    }

    // Public API to fetch the current RestaurantInfo (no need to know its ID)
    @GetMapping("/current")
    public RestaurantInfo getCurrentRestaurantInfo() {
        return restaurantInfoRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No restaurant info found"));
    }
}
