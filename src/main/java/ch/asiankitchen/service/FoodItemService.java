package ch.asiankitchen.service;

import ch.asiankitchen.dto.FoodItemDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class FoodItemService {
    private final FoodItemRepository repo;

    public FoodItemService(FoodItemRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public FoodItemDTO create(FoodItemDTO dto) {
        FoodItem entity = dto.toEntity();
        return FoodItemDTO.fromEntity(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<FoodItemDTO> listAll() {
        return repo.findAll().stream()
                .map(FoodItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FoodItemDTO getById(UUID id) {
        return repo.findById(id)
                .map(FoodItemDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", id));
    }

    @Transactional
    public FoodItemDTO update(UUID id, FoodItemDTO dto) {
        FoodItem existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setIngredients(dto.getIngredients());
        existing.setAllergies(dto.getAllergies());
        existing.setImageUrl(dto.getImageUrl());
        return FoodItemDTO.fromEntity(repo.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}