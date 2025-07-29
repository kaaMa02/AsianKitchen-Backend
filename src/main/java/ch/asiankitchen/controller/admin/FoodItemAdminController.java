package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.FoodItemDTO;
import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.repository.FoodItemRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/food-items")
public class FoodItemAdminController {

    private final FoodItemRepository foodItemRepository;

    public FoodItemAdminController(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    @GetMapping
    public List<FoodItemDTO> getAll() {
        return foodItemRepository.findAll().stream()
                .map(FoodItemDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public FoodItemDTO getById(@PathVariable UUID id) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found: " + id));
        return FoodItemDTO.fromEntity(item);
    }

    @PostMapping
    public FoodItemDTO create(@Valid @RequestBody FoodItemDTO dto) {
        FoodItem saved = foodItemRepository.save(dto.toEntity());
        return FoodItemDTO.fromEntity(saved);
    }

    @PutMapping("/{id}")
    public FoodItemDTO update(@PathVariable UUID id, @Valid @RequestBody FoodItemDTO dto) {
        FoodItem existing = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setIngredients(dto.getIngredients());
        existing.setAllergies(dto.getAllergies());
        existing.setImageUrl(dto.getImageUrl());

        return FoodItemDTO.fromEntity(foodItemRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        foodItemRepository.deleteById(id);
    }
}
