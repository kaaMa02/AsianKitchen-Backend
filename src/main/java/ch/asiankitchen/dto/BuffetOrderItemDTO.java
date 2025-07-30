package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.model.BuffetOrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BuffetOrderItemDTO {
    @NotNull
    private UUID buffetItemId;

    @Min(1)
    private int quantity;

    public BuffetOrderItem toEntity() {
        return BuffetOrderItem.builder()
                .buffetItem(BuffetItem.builder().id(buffetItemId).build())
                .quantity(quantity)
                .build();
    }
}
