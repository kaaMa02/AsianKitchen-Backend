package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.MenuItemDTO;
import ch.asiankitchen.dto.MenuItemWriteDTO;
import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.repository.FoodItemRepository;
import ch.asiankitchen.repository.MenuItemRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/menu-items")
public class MenuItemAdminController {

    private final MenuItemRepository menuRepo;
    private final FoodItemRepository foodRepo;

    public MenuItemAdminController(MenuItemRepository menuRepo, FoodItemRepository foodRepo) {
        this.menuRepo = menuRepo;
        this.foodRepo = foodRepo;
    }

    @GetMapping
    public List<MenuItemDTO> getAllMenuItems() {
        return menuRepo.findAll().stream()
                .map(MenuItemDTO::fromEntity)
                .toList();
    }

    @PostMapping
    public MenuItemDTO createMenuItem(@Valid @RequestBody MenuItemWriteDTO dto) {
        FoodItem foodItem = foodRepo.findById(dto.getFoodItemId())
        .orElseThrow(() -> new IllegalArgumentException("Food item not found"));

        MenuItem menuItem = MenuItem.builder()
                .foodItem(foodItem)
                .category(dto.getCategory())
                .available(dto.isAvailable())
                .price(dto.getPrice())
                .build();

        return MenuItemDTO.fromEntity(menuRepo.save(menuItem));
    }

    @PutMapping("/{id}")
    public MenuItemDTO update(@PathVariable UUID id, @Valid @RequestBody MenuItemWriteDTO dto) {
        MenuItem item = menuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        FoodItem food = foodRepo.findById(dto.getFoodItemId())
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        item.setFoodItem(food);
        item.setCategory(dto.getCategory());
        item.setAvailable(dto.isAvailable());
        item.setPrice(dto.getPrice());

        return MenuItemDTO.fromEntity(menuRepo.save(item));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        menuRepo.deleteById(id);
    }
}
