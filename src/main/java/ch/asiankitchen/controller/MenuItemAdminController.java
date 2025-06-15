package ch.asiankitchen.controller;

import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/menu-items")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuItemAdminController {

    private final MenuItemRepository menuItemRepository;

    public MenuItemAdminController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @PostMapping
    public MenuItem createMenuItem(@RequestBody MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable UUID id, @RequestBody MenuItem updatedItem) {
        MenuItem existing = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        existing.setFoodItem(updatedItem.getFoodItem());
        existing.setCategory(updatedItem.getCategory());
        existing.setAvailable(updatedItem.isAvailable());
        existing.setPrice(updatedItem.getPrice());
        return menuItemRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteMenuItem(@PathVariable UUID id) {
        menuItemRepository.deleteById(id);
    }
}
