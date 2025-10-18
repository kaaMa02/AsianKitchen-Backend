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
public class CustomerOrderReadDTO {
    private UUID id;
    private CustomerInfoDTO customerInfo;
    private OrderType orderType;
    private OrderStatus status;
    private List<OrderItemReadDTO> orderItems;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String specialInstructions;
    private PaymentStatus paymentStatus;
    private String paymentIntentId;
    private PaymentMethod paymentMethod;

    // snapshot fields (null-safe in mapper)
    private BigDecimal itemsSubtotalBeforeDiscount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal itemsSubtotalAfterDiscount;
    private BigDecimal vatAmount;
    private BigDecimal deliveryFee;

    private OrderTimingReadDTO timing;

    public static CustomerOrderReadDTO fromEntity(CustomerOrder o) {
        return CustomerOrderReadDTO.builder()
                .id(o.getId())
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .orderItems(Optional.ofNullable(o.getOrderItems())
                        .orElse(List.of())
                        .stream()
                        .map(OrderItemReadDTO::fromEntity)
                        .toList())
                .totalPrice(opt(o.getTotalPrice()))
                .createdAt(o.getCreatedAt())
                .specialInstructions(o.getSpecialInstructions())
                .paymentStatus(o.getPaymentStatus())
                .paymentIntentId(o.getPaymentIntentId())
                .paymentMethod(o.getPaymentMethod())
                .itemsSubtotalBeforeDiscount(opt(o.getItemsSubtotalBeforeDiscount()))
                .discountPercent(optPct(o.getDiscountPercent()))
                .discountAmount(opt(o.getDiscountAmount()))
                .itemsSubtotalAfterDiscount(opt(o.getItemsSubtotalAfterDiscount()))
                .vatAmount(opt(o.getVatAmount()))
                .deliveryFee(opt(o.getDeliveryFee()))
                .timing(OrderTimingReadDTO.builder()
                        .asap(o.isAsap())
                        .requestedAt(o.getRequestedAt())
                        .minPrepMinutes(o.getMinPrepMinutes())
                        .adminExtraMinutes(o.getAdminExtraMinutes())
                        .committedReadyAt(o.getCommittedReadyAt())
                        .autoCancelAt(o.getAutoCancelAt())
                        .seenAt(o.getSeenAt())
                        .escalatedAt(o.getEscalatedAt())
                        .build())
                .build();
    }

    private static BigDecimal opt(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }
    private static BigDecimal optPct(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP));
    }
}
