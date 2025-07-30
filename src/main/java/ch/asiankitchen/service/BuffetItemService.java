package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.repository.BuffetItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class BuffetItemService {
    private final BuffetItemRepository repo;

    public BuffetItemService(BuffetItemRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public BuffetItemReadDTO create(BuffetItemWriteDTO dto) {
        var item = BuffetItem.builder()
                .foodItem(ch.asiankitchen.model.FoodItem.builder().id(dto.getFoodItemId()).build())
                .available(dto.isAvailable())
                .price(dto.getPrice())
                .build();
        return BuffetItemReadDTO.fromEntity(repo.save(item));
    }

    @Transactional(readOnly = true)
    public List<BuffetItemReadDTO> listAll() {
        return repo.findAll().stream()
                .map(BuffetItemReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuffetItemReadDTO update(UUID id, BuffetItemWriteDTO dto) {
        var item = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetItem", id));
        item.setAvailable(dto.isAvailable());
        item.setPrice(dto.getPrice());
        return BuffetItemReadDTO.fromEntity(repo.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BuffetItemReadDTO> listAvailable() {
        return repo.findByAvailableTrue().stream()
                .map(BuffetItemReadDTO::fromEntity)
                .toList();
    }
}
