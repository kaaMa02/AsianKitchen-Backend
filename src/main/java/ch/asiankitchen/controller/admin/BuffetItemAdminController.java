package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.BuffetItemReadDTO;
import ch.asiankitchen.dto.BuffetItemWriteDTO;
import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.repository.BuffetItemRepository;
import ch.asiankitchen.repository.FoodItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/buffet-items")
public class BuffetItemAdminController {

    private final BuffetItemRepository buffetItemRepository;
    private final FoodItemRepository foodItemRepository;

    public BuffetItemAdminController(BuffetItemRepository buffetItemRepository,
                                     FoodItemRepository foodItemRepository) {
        this.buffetItemRepository = buffetItemRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @GetMapping
    public List<BuffetItemReadDTO> getAllBuffetItems() {
        return buffetItemRepository.findAll()
                .stream()
                .map(item -> BuffetItemReadDTO.builder()
                        .id(item.getId())
                        .available(item.isAvailable())
                        .foodItemId(item.getFoodItem().getId())
                        .foodItemName(item.getFoodItem().getName())
                        .build())
                .toList();
    }

    @PostMapping
    public BuffetItemReadDTO createBuffetItem(@RequestBody BuffetItemWriteDTO dto) {
        FoodItem foodItem = foodItemRepository.findById(dto.getFoodItemId())
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        BuffetItem buffetItem = BuffetItem.builder()
                .available(dto.isAvailable())
                .foodItem(foodItem)
                .build();

        BuffetItem saved = buffetItemRepository.save(buffetItem);

        return BuffetItemReadDTO.builder()
                .id(saved.getId())
                .available(saved.isAvailable())
                .foodItemId(foodItem.getId())
                .foodItemName(foodItem.getName())
                .build();
    }

    @PutMapping("/{id}")
    public BuffetItemReadDTO updateBuffetItem(@PathVariable UUID id, @RequestBody BuffetItemWriteDTO dto) {
        BuffetItem existing = buffetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuffetItem not found"));

        FoodItem foodItem = foodItemRepository.findById(dto.getFoodItemId())
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        existing.setAvailable(dto.isAvailable());
        existing.setFoodItem(foodItem);

        BuffetItem saved = buffetItemRepository.save(existing);

        return BuffetItemReadDTO.builder()
                .id(saved.getId())
                .available(saved.isAvailable())
                .foodItemId(foodItem.getId())
                .foodItemName(foodItem.getName())
                .build();
    }

    @DeleteMapping("/{id}")
    public void deleteBuffetItem(@PathVariable UUID id) {
        buffetItemRepository.deleteById(id);
    }
}
