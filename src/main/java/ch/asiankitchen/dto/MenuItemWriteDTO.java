package ch.asiankitchen.dto;

import ch.asiankitchen.model.FoodItem;
import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.model.MenuItemCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@Data
@Builder
public class MenuItemWriteDTO {
    @NotNull
    private UUID foodItemId;

    @NotNull private MenuItemCategory category;
    private boolean available;

    @NotNull
    @PositiveOrZero
    private Double price;

    public MenuItem toEntity() {
        return ch.asiankitchen.model.MenuItem.builder()
                .foodItem(FoodItem.builder().id(foodItemId).build())
                .category(category)
                .available(available)
                .price(price)
                .build();
    }
}
