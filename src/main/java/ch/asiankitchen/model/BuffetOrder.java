package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "buffet_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetOrder {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    private CustomerInfo customerInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Lob
    @Column(name = "special_instructions")
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "buffetOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuffetOrderItem> buffetOrderItems = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        recalcTotal();
    }

    @PreUpdate
    public void onUpdate() {
        recalcTotal();
    }

    private void recalcTotal() {
        this.totalPrice = buffetOrderItems.stream()
                .map(item ->
                        item.getBuffetItem()
                                .getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalPrice = this.totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
}
