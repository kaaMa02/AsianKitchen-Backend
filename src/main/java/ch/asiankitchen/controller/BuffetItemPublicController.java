package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetItemReadDTO;
import ch.asiankitchen.repository.BuffetItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buffet-items")
public class BuffetItemPublicController {

    private final BuffetItemRepository buffetItemRepository;

    public BuffetItemPublicController(BuffetItemRepository buffetItemRepository) {
        this.buffetItemRepository = buffetItemRepository;
    }

    @GetMapping
    public List<BuffetItemReadDTO> getAvailableBuffetItems() {
        return buffetItemRepository.findByAvailableTrue()
                .stream()
                .map(item -> BuffetItemReadDTO.builder()
                        .id(item.getId())
                        .available(item.isAvailable())
                        .foodItemId(item.getFoodItem().getId())
                        .foodItemName(item.getFoodItem().getName())
                        .build())
                .toList();
    }

}
