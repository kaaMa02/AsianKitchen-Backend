package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BuffetOrderWriteDTO {

    private UUID userId;

    @NotNull
    @Valid
    private CustomerInfo customerInfo;

    @NotNull
    private OrderType orderType;

    private String specialInstructions;

    @NotEmpty
    @Valid
    private List<BuffetOrderItemDTO> buffetOrderItems;

    private double totalPrice;

    public BuffetOrder toEntity() {
        BuffetOrder order = new BuffetOrder();
        order.setUser(userId != null ? User.builder().id(userId).build() : null);
        order.setCustomerInfo(customerInfo);
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        order.setTotalPrice(totalPrice);
        order.setBuffetOrderItems(
                buffetOrderItems.stream().map(BuffetOrderItemDTO::toEntity).toList()
        );
        return order;
    }

    @Data
    public static class BuffetOrderItemDTO {

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
}
