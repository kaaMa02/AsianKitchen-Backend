package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.*;

@Service
public class CustomerOrderService {
    private final CustomerOrderRepository repo;
    private final MenuItemRepository menuItemRepo;

    public CustomerOrderService(CustomerOrderRepository repo, MenuItemRepository menuItemRepo) {
        this.repo = repo;
        this.menuItemRepo = menuItemRepo;
    }

    @Transactional
    public CustomerOrderReadDTO create(CustomerOrderWriteDTO dto) {
        var order = dto.toEntity();
        order.setStatus(OrderStatus.NEW); // paymentStatus defaults in @PrePersist

        // Attach real MenuItem entities (and sanity checks)
        order.getOrderItems().forEach(oi -> {
            var id = oi.getMenuItem().getId(); // comes from OrderItemWriteDTO
            MenuItem mi = menuItemRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
            if (!mi.isAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + mi.getId());
            }
            oi.setMenuItem(mi);
            oi.setCustomerOrder(order); // ensure backref
        });

        // Compute total from DB prices (server-truth)
        order.setTotalPrice(computeTotal(order));

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

    private BigDecimal computeTotal(CustomerOrder order) {
        return order.getOrderItems().stream()
                .map(oi -> {
                    BigDecimal price = oi.getMenuItem().getPrice();
                    if (price == null) price = BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
