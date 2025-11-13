package ch.asiankitchen.dto;

import ch.asiankitchen.json.FlexibleLocalDateTimeDeserializer;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderType;
import ch.asiankitchen.model.PaymentMethod;
import ch.asiankitchen.model.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private Boolean asap;               // default true

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime scheduledAt;  // used if asap=false

    public void setItems(List<BuffetOrderItemWriteDTO> items) {
        this.items = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
    }

    public BuffetOrder toEntity() {
        var order = new BuffetOrder();
        if (userId != null) order.setUser(User.builder().id(userId).build());
        order.setCustomerInfo(CustomerInfoDTO.toEntity(customerInfo));
        order.setOrderType(orderType);
        order.setSpecialInstructions(specialInstructions);
        var list = items.stream()
                .map(dto -> {
                    var item = dto.toEntity();
                    item.setBuffetOrder(order);
                    return item;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        order.setBuffetOrderItems(list);
        order.setPaymentMethod(paymentMethod);
        order.setAsap(asap == null || asap);
        if (Boolean.FALSE.equals(asap)) order.setRequestedAt(scheduledAt);
        return order;
    }

}