package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    // snapshot fields (null-safe)
    private BigDecimal itemsSubtotalBeforeDiscount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal itemsSubtotalAfterDiscount;
    private BigDecimal vatAmount;
    private BigDecimal deliveryFee;

    public static BuffetOrderReadDTO fromEntity(BuffetOrder o) {
        return BuffetOrderReadDTO.builder()
                .id(o.getId())
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .totalPrice(opt(o.getTotalPrice()))
                .specialInstructions(o.getSpecialInstructions())
                .paymentStatus(o.getPaymentStatus())
                .paymentIntentId(o.getPaymentIntentId())
                .orderItems(Optional.ofNullable(o.getBuffetOrderItems())
                        .orElse(List.of())
                        .stream()
                        .map(BuffetOrderItemReadDTO::fromEntity)
                        .toList())
                .paymentMethod(o.getPaymentMethod())
                .itemsSubtotalBeforeDiscount(opt(o.getItemsSubtotalBeforeDiscount()))
                .discountPercent(optPct(o.getDiscountPercent()))
                .discountAmount(opt(o.getDiscountAmount()))
                .itemsSubtotalAfterDiscount(opt(o.getItemsSubtotalAfterDiscount()))
                .vatAmount(opt(o.getVatAmount()))
                .deliveryFee(opt(o.getDeliveryFee()))
                .build();
    }

    private static BigDecimal opt(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }
    private static BigDecimal optPct(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP));
    }
}