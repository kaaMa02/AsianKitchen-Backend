package ch.asiankitchen.dto;

import ch.asiankitchen.model.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data @Builder
public class NewOrderCardDTO {
    private UUID id;
    private String kind; // "menu" | "buffet" | "reservation"
    private CustomerInfoDTO customerInfo;

    private OrderType orderType;     // orders only
    private OrderStatus status;      // or ReservationStatus for reservations (map to pseudo)
    private LocalDateTime createdAt;
    private String specialInstructions;

    private OrderTimingReadDTO timing;

    private PaymentStatus paymentStatus; // orders only
    private PaymentMethod paymentMethod;
    private String paymentIntentId;

    private BigDecimal totalPrice;

    private List<OrderItemReadDTO> menuItems;
    private List<BuffetOrderItemReadDTO> buffetItems;
}
