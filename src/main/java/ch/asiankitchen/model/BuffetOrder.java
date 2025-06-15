package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetOrder {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Embedded
    private CustomerInfo customerInfo;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "buffetOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuffetOrderItem> buffetOrderItems = new ArrayList<>();

    private LocalDateTime createdAt;
    private double totalPrice;

    @Lob
    private String specialInstructions;
}
