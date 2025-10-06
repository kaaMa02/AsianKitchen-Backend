package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BuffetOrderReadDTO {
    private UUID id;
    private CustomerInfoDTO customerInfo;
    private OrderType orderType;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private String specialInstructions;
    private PaymentStatus paymentStatus;
    private String paymentIntentId;
    private List<BuffetOrderItemReadDTO> orderItems;
    private PaymentMethod paymentMethod;

    public static BuffetOrderReadDTO fromEntity(BuffetOrder o) {
        return BuffetOrderReadDTO.builder()
                .id(o.getId())
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice())
                .specialInstructions(o.getSpecialInstructions())
                .paymentStatus(o.getPaymentStatus())
                .paymentIntentId(o.getPaymentIntentId())
                .orderItems(
                        o.getBuffetOrderItems().stream()
                                .map(BuffetOrderItemReadDTO::fromEntity).toList()
                )
                .paymentMethod(o.getPaymentMethod())
                .build();
    }
}
