package ch.asiankitchen.controller;

import ch.asiankitchen.dto.FoodItemDTO;
import ch.asiankitchen.service.FoodItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/food-items")
public class FoodItemPublicController {
    private final FoodItemService service;

    public FoodItemPublicController(FoodItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<FoodItemDTO> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FoodItemDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }
}
