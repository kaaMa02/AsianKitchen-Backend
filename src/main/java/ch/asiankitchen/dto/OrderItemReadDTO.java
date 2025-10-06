package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemReadDTO {
    private UUID id;
    private UUID menuItemId;
    private String menuItemName;
    private BigDecimal unitPrice;
    private int quantity;


    public static OrderItemReadDTO fromEntity(OrderItem item) {
        var mi = item.getMenuItem();
        return OrderItemReadDTO.builder()
                .id(item.getId())
                .menuItemId(mi != null ? mi.getId() : null)
                .menuItemName(mi != null && mi.getFoodItem() != null ? mi.getFoodItem().getName() : null)
                .unitPrice(mi != null ? mi.getPrice() : null)
                .quantity(item.getQuantity())
                .build();
    }
}
