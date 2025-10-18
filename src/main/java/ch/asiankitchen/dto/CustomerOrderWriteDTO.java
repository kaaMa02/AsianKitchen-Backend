package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerOrderWriteDTO {
    private UUID userId;

    @NotNull @Valid private CustomerInfoDTO customerInfo;
    @NotNull private OrderType orderType;
    private String specialInstructions;
    @NotNull private PaymentMethod paymentMethod;
    @NotEmpty @Valid private List<OrderItemWriteDTO> items;

    private Boolean asap;               // default true
    private LocalDateTime scheduledAt;  // required if asap == false

    public CustomerOrder toEntity() {
        var order = new CustomerOrder();
        if (userId != null) order.setUser(User.builder().id(userId).build());
        order.setCustomerInfo(CustomerInfoDTO.toEntity(customerInfo));
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        order.setPaymentMethod(paymentMethod);
        order.setOrderItems(items.stream().map(dto -> {
            var item = dto.toEntity();
            item.setCustomerOrder(order);
            return item;
        }).toList());

        // timing defaults; final computation happens in service
        order.setAsap(asap == null || asap);
        if (Boolean.FALSE.equals(asap)) order.setRequestedAt(scheduledAt);
        return order;
    }
}
