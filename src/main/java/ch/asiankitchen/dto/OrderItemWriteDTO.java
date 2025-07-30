package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
public class OrderItemWriteDTO {
    @NotNull
    private UUID menuItemId;

    @Min(1)
    private int quantity;

    public OrderItem toEntity() {
        return OrderItem.builder()
                .menuItem(MenuItem.builder().id(menuItemId).build())
                .quantity(quantity)
                .build();
    }
}
