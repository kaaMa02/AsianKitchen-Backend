package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerOrderReadDTO {
    private UUID id;
    private CustomerInfoDTO customerInfo;
    private OrderType orderType;
    private OrderStatus status;
    private List<OrderItemReadDTO> orderItems;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String specialInstructions;

    public static CustomerOrderReadDTO fromEntity(CustomerOrder o) {
        return CustomerOrderReadDTO.builder()
                .id(o.getId())
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .orderItems(o.getOrderItems().stream()
                        .map(OrderItemReadDTO::fromEntity)
                        .toList())
                .totalPrice(o.getTotalPrice())
                .createdAt(o.getCreatedAt())
                .specialInstructions(o.getSpecialInstructions())
                .build();
    }
}
