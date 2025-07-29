package ch.asiankitchen.dto;

import ch.asiankitchen.model.MenuItemCategory;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemWriteDTO {
    private UUID foodItemId;
    private MenuItemCategory category;
    private boolean available;
    private double price;
}
