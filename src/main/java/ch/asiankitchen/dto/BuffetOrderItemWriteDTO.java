package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.model.BuffetOrderItem;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BuffetOrderItemWriteDTO {
    @NotNull
    private UUID buffetItemId;
    private int quantity;

    public BuffetOrderItem toEntity() {
        return BuffetOrderItem.builder()
                .buffetItem(BuffetItem.builder().id(buffetItemId).build())
                .quantity(quantity)
                .build();
    }
}
