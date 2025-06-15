package ch.asiankitchen.controller;

import ch.asiankitchen.model.RestaurantInfo;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurant-info")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantInfoAdminController {

    private final RestaurantInfoRepository restaurantInfoRepo;

    public RestaurantInfoAdminController(RestaurantInfoRepository restaurantInfoRepo) {
        this.restaurantInfoRepo = restaurantInfoRepo;
    }

    @PostMapping
    public RestaurantInfo create(@RequestBody RestaurantInfo info) {
        if (!restaurantInfoRepo
                .findAll()
                .isEmpty())
            throw new RuntimeException("Exists");
        return restaurantInfoRepo.save(info);
    }

    @PutMapping("/{id}")
    public RestaurantInfo update(
            @PathVariable UUID id, @RequestBody RestaurantInfo u) {
        RestaurantInfo e = restaurantInfoRepo.findById(id).orElseThrow();
        e.setName(u.getName());
        e.setAboutText(u.getAboutText());
        e.setAddress(u.getAddress());
        e.setPhone(u.getPhone());
        e.setEmail(u.getEmail());
        e.setInstagramUrl(u.getInstagramUrl());
        e.setGoogleMapsUrl(u.getGoogleMapsUrl());
        e.setOpeningHours(u.getOpeningHours());
        return restaurantInfoRepo.save(e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        restaurantInfoRepo.deleteById(id);
    }
}
