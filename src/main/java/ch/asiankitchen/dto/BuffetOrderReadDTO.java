package ch.asiankitchen.dto;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BuffetOrderReadDTO {
    private UUID id;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    private String paymentIntentId;

    public static BuffetOrderReadDTO fromEntity(BuffetOrder o) {
        var ci = o.getCustomerInfo();
        return BuffetOrderReadDTO.builder()
                .id(o.getId())
                .customerName(ci.getFirstName() + " " + ci.getLastName())
                .customerEmail(ci.getEmail())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice())
                .paymentStatus(o.getPaymentStatus())
                .paymentIntentId(o.getPaymentIntentId())
                .build();
    }
}