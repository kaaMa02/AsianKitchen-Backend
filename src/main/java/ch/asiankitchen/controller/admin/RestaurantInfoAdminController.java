package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.dto.RestaurantInfoWriteDTO;
import ch.asiankitchen.model.RestaurantInfo;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurant-info")
public class RestaurantInfoAdminController {

    private final RestaurantInfoRepository restaurantInfoRepo;

    public RestaurantInfoAdminController(RestaurantInfoRepository restaurantInfoRepo) {
        this.restaurantInfoRepo = restaurantInfoRepo;
    }

    @PostMapping
    public ResponseEntity<RestaurantInfoReadDTO> create(@Valid @RequestBody RestaurantInfoWriteDTO dto) {
        if (!restaurantInfoRepo.findAll().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Only one record allowed
        }

        RestaurantInfo saved = restaurantInfoRepo.save(dto.toEntity());
        return ResponseEntity.ok(RestaurantInfoReadDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantInfoReadDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantInfoWriteDTO dto) {

        RestaurantInfo existing = restaurantInfoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant info not found"));

        existing.setName(dto.getName());
        existing.setAboutText(dto.getAboutText());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setInstagramUrl(dto.getInstagramUrl());
        existing.setGoogleMapsUrl(dto.getGoogleMapsUrl());
        existing.setOpeningHours(dto.getOpeningHours());
        existing.setAddress(dto.getAddress());

        RestaurantInfo updated = restaurantInfoRepo.save(existing);
        return ResponseEntity.ok(RestaurantInfoReadDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public void deleteRestaurantInfo(@PathVariable UUID id) {
        restaurantInfoRepo.deleteById(id);
    }
}
