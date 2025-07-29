package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemReadDTO {
    private UUID id;
    private UUID menuItemId;
    private int quantity;

    public static OrderItemReadDTO fromEntity(OrderItem item) {
        return OrderItemReadDTO.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .quantity(item.getQuantity())
                .build();
    }
}
