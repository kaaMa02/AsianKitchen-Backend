package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "buffet_order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetOrderItem {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buffet_order_id", nullable = false)
    private BuffetOrder buffetOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buffet_item_id", nullable = false)
    private BuffetItem buffetItem;

    @Min(1)
    @Column(nullable = false)
    private int quantity;
}
