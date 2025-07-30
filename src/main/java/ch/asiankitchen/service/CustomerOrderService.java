package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.CustomerOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class CustomerOrderService {
    private final CustomerOrderRepository repo;

    public CustomerOrderService(CustomerOrderRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public CustomerOrderReadDTO create(CustomerOrderWriteDTO dto) {
        var order = dto.toEntity();
        order.setStatus(OrderStatus.NEW);
        // createdAt via @PrePersist
        var saved = repo.save(order);
        return CustomerOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
        order.setStatus(status);
        return CustomerOrderReadDTO.fromEntity(repo.save(order));
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO track(UUID id, String email) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
