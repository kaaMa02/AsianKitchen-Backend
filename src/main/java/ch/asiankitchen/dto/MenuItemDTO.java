package ch.asiankitchen.dto;

import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.model.MenuItemCategory;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MenuItemDTO {
    private UUID id;
    private UUID foodItemId;
    private MenuItemCategory category;
    private boolean available;
    private BigDecimal price;

    public static MenuItemDTO fromEntity(MenuItem m) {
        return MenuItemDTO.builder()
                .id(m.getId())
                .foodItemId(m.getFoodItem().getId())
                .category(m.getCategory())
                .available(m.isAvailable())
                .price(m.getPrice())
                .build();
    }
}
