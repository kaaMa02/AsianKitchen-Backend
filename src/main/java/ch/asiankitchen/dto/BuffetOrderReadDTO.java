package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BuffetOrderReadDTO {

    private UUID id;
    private String customerName;
    private String customerEmail;
    private Status status;
    private LocalDateTime createdAt;
    private double totalPrice;

    public static BuffetOrderReadDTO fromEntity(BuffetOrder order) {
        return BuffetOrderReadDTO.builder()
                .id(order.getId())
                .customerName(order.getCustomerInfo() != null
                        ? order.getCustomerInfo().getFirstName() + " " + order.getCustomerInfo().getLastName()
                        : "Anonymous")
                .customerEmail(order.getCustomerInfo() != null
                        ? order.getCustomerInfo().getEmail()
                        : "")
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
