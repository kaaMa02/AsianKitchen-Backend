package ch.asiankitchen.controller;

import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.repository.BuffetItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/buffet-items")
@CrossOrigin(origins = "http://localhost:3000")
public class BuffetItemAdminController {

    private final BuffetItemRepository buffetItemRepository;

    public BuffetItemAdminController(BuffetItemRepository buffetItemRepository) {
        this.buffetItemRepository = buffetItemRepository;
    }

    @GetMapping
    public List<BuffetItem> getAllBuffetItems() {
        return buffetItemRepository.findAll();
    }

    @PostMapping
    public BuffetItem createBuffetItem(@RequestBody BuffetItem buffetItem) {
        return buffetItemRepository.save(buffetItem);
    }

    @PutMapping("/{id}")
    public BuffetItem updateBuffetItem(@PathVariable UUID id, @RequestBody BuffetItem updatedItem) {
        BuffetItem existing = buffetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuffetItem not found"));

        existing.setFoodItem(updatedItem.getFoodItem());
        existing.setAvailable(updatedItem.isAvailable());
        return buffetItemRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteBuffetItem(@PathVariable UUID id) {
        buffetItemRepository.deleteById(id);
    }
}
