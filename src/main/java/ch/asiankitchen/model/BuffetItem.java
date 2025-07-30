package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "buffet_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetItem {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private Double price;
}
