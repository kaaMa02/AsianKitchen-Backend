package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetItem;
import ch.asiankitchen.model.BuffetOrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BuffetOrderItemReadDTO {
    @NotNull
    private UUID buffetItemId;
    private String name;
    private BigDecimal unitPrice;
    private int quantity;

    public static BuffetOrderItemReadDTO fromEntity(BuffetOrderItem it) {
        var bi = it.getBuffetItem();
        return BuffetOrderItemReadDTO.builder()
                .buffetItemId(bi.getId())
                .name(bi.getFoodItem()!=null ? bi.getFoodItem().getName() : null)
                .unitPrice(bi.getPrice())
                .quantity(it.getQuantity())
                .build();
    }
}
