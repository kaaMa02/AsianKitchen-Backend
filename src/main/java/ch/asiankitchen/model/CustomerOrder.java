package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    @Column(name = "items_subtotal_before_discount", precision = 10, scale = 2)
    private BigDecimal itemsSubtotalBeforeDiscount;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "items_subtotal_after_discount", precision = 10, scale = 2)
    private BigDecimal itemsSubtotalAfterDiscount;

    @Column(name = "vat_amount", precision = 10, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    // -------- NEW timing fields --------
    @Column(name = "asap", nullable = false)
    private boolean asap = true;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "min_prep_minutes", nullable = false)
    private Integer minPrepMinutes = 45;

    @Column(name = "admin_extra_minutes", nullable = false)
    private Integer adminExtraMinutes = 0;

    @Column(name = "committed_ready_at")
    private LocalDateTime committedReadyAt;

    @Column(name = "auto_cancel_at")
    private LocalDateTime autoCancelAt;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    public void setOrderItems(List<OrderItem> items) {
        this.orderItems = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
        if (paymentStatus == null) paymentStatus = PaymentStatus.REQUIRES_PAYMENT_METHOD;
        if (minPrepMinutes == null) minPrepMinutes = 45;
        if (adminExtraMinutes == null) adminExtraMinutes = 0;
        if (status == null) status = OrderStatus.NEW;
    }
}
