package ch.asiankitchen.service;

import ch.asiankitchen.dto.CustomerOrderReadDTO;
import ch.asiankitchen.dto.CustomerOrderWriteDTO;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.CustomerOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    @Transactional
    public CustomerOrderReadDTO createOrder(CustomerOrderWriteDTO dto) {
        CustomerOrder customerOrder = dto.toEntity();

        customerOrder.setCreatedAt(LocalDateTime.now());
        customerOrder.setStatus(Status.ORDER_NEW);
        customerOrder.getOrderItems().forEach(item -> item.setCustomerOrder(customerOrder));

        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
        return CustomerOrderReadDTO.fromEntity(savedOrder);
    }

    public List<CustomerOrderReadDTO> getOrdersByUserId(UUID userId) {
        return customerOrderRepository.findByUserId(userId)
                .stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CustomerOrderReadDTO updateStatus(UUID id, Status newStatus) {
        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerOrder not found"));

        order.setStatus(newStatus);
        return CustomerOrderReadDTO.fromEntity(customerOrderRepository.save(order));
    }

    public CustomerOrderReadDTO trackOrder(UUID orderId, String email) {
        return customerOrderRepository.findById(orderId)
                .filter(o -> o.getCustomerInfo() != null &&
                        o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("No matching order found"));
    }

    public List<CustomerOrderReadDTO> findAll() {
        return customerOrderRepository.findAll()
                .stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
