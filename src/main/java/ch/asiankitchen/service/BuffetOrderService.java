package ch.asiankitchen.service;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.dto.BuffetOrderWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class BuffetOrderService {
    private final BuffetOrderRepository repo;

    public BuffetOrderService(BuffetOrderRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public BuffetOrderReadDTO create(BuffetOrderWriteDTO dto) {
        var saved = repo.save(dto.toEntity());
        return BuffetOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listPaidNew() {
        return repo.findByStatusAndPaymentStatusOrderByCreatedAtDesc(
                OrderStatus.NEW, PaymentStatus.SUCCEEDED
        ).stream().map(BuffetOrderReadDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO track(UUID id, String email) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    @Transactional
    public BuffetOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
        order.setStatus(status);
        return BuffetOrderReadDTO.fromEntity(repo.save(order));
    }
}