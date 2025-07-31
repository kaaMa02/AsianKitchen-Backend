package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemCategory category;

    @Column(nullable = false)
    private boolean available = true;

    @NotNull
    @PositiveOrZero
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
}
