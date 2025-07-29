package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderWriteDTO {

    private UUID userId;
    private CustomerInfo customerInfo;
    private OrderType orderType;
    private String specialInstructions;
    private List<OrderItemWriteDTO> orderItems;

    public CustomerOrder toEntity() {
        CustomerOrder order = new CustomerOrder();
        order.setUser(userId != null ? User.builder().id(userId).build() : null);
        order.setCustomerInfo(customerInfo);
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        order.setStatus(Status.ORDER_NEW);
        order.setCreatedAt(java.time.LocalDateTime.now());

        List<OrderItem> itemList = orderItems.stream()
                .map(itemDTO -> {
                    OrderItem item = itemDTO.toEntity();
                    item.setCustomerOrder(order);
                    return item;
                }).toList();

        order.setOrderItems(itemList);
        return order;
    }
}
