package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;

    @Lob
    private String description;

    @Lob
    private String ingredients;

    @Lob
    private String allergies;

    private String imageUrl;
}
