package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerOrderWriteDTO {

    private UUID userId;

    @NotNull
    @Valid
    private CustomerInfoDTO customerInfo;

    @NotNull
    private OrderType orderType;

    private String specialInstructions;

    @NotEmpty
    @Valid
    private List<OrderItemWriteDTO> items;

    public CustomerOrder toEntity() {
        var order = new CustomerOrder();
        if (userId != null) {
            order.setUser(User.builder().id(userId).build());
        }
        order.setCustomerInfo(CustomerInfoDTO.toEntity(customerInfo));
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        order.setOrderItems(items.stream().map(dto -> {
            var item = dto.toEntity();
            item.setCustomerOrder(order);
            return item;
        }).toList());
        return order;
    }
}
