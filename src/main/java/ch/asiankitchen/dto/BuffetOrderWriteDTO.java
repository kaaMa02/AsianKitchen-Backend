package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderType;
import ch.asiankitchen.model.PaymentMethod;
import ch.asiankitchen.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BuffetOrderWriteDTO {
    private UUID userId;

    @NotNull
    @Valid
    private CustomerInfoDTO customerInfo;

    @NotNull
    private OrderType orderType;

    private String specialInstructions;

    @NotEmpty
    @Valid
    private List<BuffetOrderItemWriteDTO> items;

    @NotNull
    private PaymentMethod paymentMethod;

    public BuffetOrder toEntity() {
        var order = new BuffetOrder();
        if (userId != null) {
            order.setUser(User.builder().id(userId).build());
        }
        order.setCustomerInfo(CustomerInfoDTO.toEntity(customerInfo));
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        order.setBuffetOrderItems(items.stream().map(dto -> {
            var item = dto.toEntity();
            item.setBuffetOrder(order);
            return item;
        }).toList());
        order.setPaymentMethod(paymentMethod);
        return order;
    }
}