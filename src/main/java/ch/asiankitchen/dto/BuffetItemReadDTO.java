package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetItem;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BuffetItemReadDTO {
    private UUID id;
    private boolean available;
    private UUID foodItemId;
    private String foodItemName;

    public static BuffetItemReadDTO fromEntity(BuffetItem i) {
        return BuffetItemReadDTO.builder()
                .id(i.getId())
                .available(i.isAvailable())
                .foodItemId(i.getFoodItem().getId())
                .foodItemName(i.getFoodItem().getName())
                .build();
    }
}