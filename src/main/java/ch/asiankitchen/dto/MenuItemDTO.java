package ch.asiankitchen.dto;

import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.model.MenuItemCategory;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {
    private UUID id;
    private UUID foodItemId;
    private MenuItemCategory category;
    private boolean available;
    private double price;

    public static MenuItemDTO fromEntity(MenuItem item) {
        return MenuItemDTO.builder()
                .id(item.getId())
                .foodItemId(item.getFoodItem().getId())
                .category(item.getCategory())
                .available(item.isAvailable())
                .price(item.getPrice())
                .build();
    }
}
