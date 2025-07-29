package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderReadDTO {
    private UUID id;
    private CustomerInfo customerInfo;
    private OrderType orderType;
    private Status status;
    private List<OrderItemReadDTO> orderItems;
    private double totalPrice;
    private LocalDateTime createdAt;
    private String specialInstructions;

    public static CustomerOrderReadDTO fromEntity(CustomerOrder o) {
        return CustomerOrderReadDTO.builder()
                .id(o.getId())
                .customerInfo(o.getCustomerInfo())
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
