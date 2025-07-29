package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemWriteDTO {
    private UUID menuItemId;
    private int quantity;

    public OrderItem toEntity() {
        return OrderItem.builder()
                .menuItem(MenuItem.builder().id(menuItemId).build())
                .quantity(quantity)
                .build();
    }
}
