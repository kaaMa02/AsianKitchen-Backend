package ch.asiankitchen.controller;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-info")
public class RestaurantInfoPublicController {

    private final RestaurantInfoRepository restaurantInfoRepository;

    public RestaurantInfoPublicController(RestaurantInfoRepository restaurantInfoRepository) {
        this.restaurantInfoRepository = restaurantInfoRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<RestaurantInfoReadDTO> getCurrentRestaurantInfo() {
        return restaurantInfoRepository.findAll()
                .stream()
                .findFirst()
                .map(RestaurantInfoReadDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
