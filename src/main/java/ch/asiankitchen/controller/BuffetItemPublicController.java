package ch.asiankitchen.controller;

import ch.asiankitchen.dto.BuffetItemReadDTO;
import ch.asiankitchen.service.BuffetItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buffet-items")
public class BuffetItemPublicController {
    private final BuffetItemService service;

    public BuffetItemPublicController(BuffetItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<BuffetItemReadDTO> listAvailable() {
        return service.listAvailable();
    }
}