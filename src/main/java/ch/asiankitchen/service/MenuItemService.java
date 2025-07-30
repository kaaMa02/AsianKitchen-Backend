package ch.asiankitchen.service;

import ch.asiankitchen.dto.MenuItemDTO;
import ch.asiankitchen.dto.MenuItemWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class MenuItemService {
    private final MenuItemRepository repo;

    public MenuItemService(MenuItemRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public MenuItemDTO create(MenuItemWriteDTO dto) {
        MenuItem entity = dto.toEntity();
        return MenuItemDTO.fromEntity(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> listAll() {
        return repo.findAll().stream()
                .map(MenuItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> listAvailable() {
        return repo.findByAvailableTrue().stream()
                .map(MenuItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemDTO getById(UUID id) {
        return repo.findById(id)
                .map(MenuItemDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    @Transactional
    public MenuItemDTO update(UUID id, MenuItemWriteDTO dto) {
        MenuItem item = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        // apply fields
        item.setCategory(dto.getCategory());
        item.setAvailable(dto.isAvailable());
        item.setPrice(dto.getPrice());
        item.setFoodItem(dto.toEntity().getFoodItem());
        return MenuItemDTO.fromEntity(repo.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
