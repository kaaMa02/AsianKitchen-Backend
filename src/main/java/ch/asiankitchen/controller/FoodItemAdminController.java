package ch.asiankitchen.controller;

import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.repository.FoodItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/food-items")
@CrossOrigin(origins = "http://localhost:3000")
public class FoodItemAdminController {

    private final FoodItemRepository foodItemRepository;

    public FoodItemAdminController(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    @GetMapping
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public FoodItem getFoodItemById(@PathVariable UUID id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found: " + id.toString()));
    }

    @PostMapping
    public FoodItem createFoodItem(@RequestBody FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    @PutMapping("/{id}")
    public FoodItem updateFoodItem(@PathVariable UUID id, @RequestBody FoodItem updatedFoodItem) {
        FoodItem existing = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FoodItem not found"));

        existing.setName(updatedFoodItem.getName());
        existing.setDescription(updatedFoodItem.getDescription());
        existing.setIngredients(updatedFoodItem.getIngredients());
        existing.setAllergies(updatedFoodItem.getAllergies());
        existing.setImageUrl(updatedFoodItem.getImageUrl());

        return foodItemRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteFoodItem(@PathVariable UUID id) {
        foodItemRepository.deleteById(id);
    }
}
