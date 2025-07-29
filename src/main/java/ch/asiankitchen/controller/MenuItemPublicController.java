package ch.asiankitchen.controller;

import ch.asiankitchen.dto.MenuItemDTO;
import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemPublicController {

    private final MenuItemRepository menuItemRepository;

    public MenuItemPublicController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    public List<MenuItemDTO> getAvailableMenuItems() {
        return menuItemRepository.findByAvailableTrue()
                .stream()
                .map(MenuItemDTO::fromEntity)
                .toList();
    }
}
