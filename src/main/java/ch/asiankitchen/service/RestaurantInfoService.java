package ch.asiankitchen.service;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.dto.RestaurantInfoWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class RestaurantInfoService {
    private final RestaurantInfoRepository repo;

    public RestaurantInfoService(RestaurantInfoRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RestaurantInfoReadDTO create(RestaurantInfoWriteDTO dto) {
        var saved = repo.save(dto.toEntity());
        return RestaurantInfoReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RestaurantInfoReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(RestaurantInfoReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantInfo", id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantInfoReadDTO> listAll() {
        return repo.findAll().stream()
                .map(RestaurantInfoReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantInfoReadDTO update(UUID id, RestaurantInfoWriteDTO dto) {
        var existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantInfo", id));
        // apply updates
        existing.setName(dto.getName());
        existing.setAboutText(dto.getAboutText());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setInstagramUrl(dto.getInstagramUrl());
        existing.setGoogleMapsUrl(dto.getGoogleMapsUrl());
        existing.setOpeningHours(dto.getOpeningHours());
        existing.setAddress(dto.getAddress().toEntity());
        return RestaurantInfoReadDTO.fromEntity(repo.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantInfoReadDTO> getCurrent() {
        return repo.findAll().stream()
                .findFirst()
                .map(RestaurantInfoReadDTO::fromEntity);
    }
}