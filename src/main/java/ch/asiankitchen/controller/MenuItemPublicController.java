package ch.asiankitchen.controller;

import ch.asiankitchen.dto.MenuItemDTO;
import ch.asiankitchen.service.MenuItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemPublicController {
    private final MenuItemService service;

    public MenuItemPublicController(MenuItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<MenuItemDTO> listAvailable() {
        return service.listAvailable();
    }
}