package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne
    private FoodItem foodItem;

    @Enumerated(EnumType.STRING)
    private MenuItemCategory category;

    private boolean available;

    private double price;
}
