package ch.asiankitchen.controller;

import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.repository.BuffetItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buffet-items")
@CrossOrigin(origins = "http://localhost:3000")
public class BuffetItemPublicController {

    private final BuffetItemRepository buffetItemRepository;

    public BuffetItemPublicController(BuffetItemRepository buffetItemRepository) {
        this.buffetItemRepository = buffetItemRepository;
    }

    @GetMapping
    public List<BuffetItem> getAvailableBuffetItems() {
        return buffetItemRepository.findByAvailableTrue();
    }
}
