package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    private CustomerInfo customerInfo;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "payment_intent_id", length = 100)
    private String paymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 30)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_method", length=30)
    private PaymentMethod paymentMethod;

    @Column(precision = 10, scale = 2)
    private BigDecimal itemsSubtotalBeforeDiscount;

    @Column(precision = 5,  scale = 2)
    private BigDecimal discountPercent;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal itemsSubtotalAfterDiscount;

    @Column(precision = 10, scale = 2)
    private BigDecimal vatAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (paymentStatus == null) paymentStatus = PaymentStatus.REQUIRES_PAYMENT_METHOD;
    }
}
