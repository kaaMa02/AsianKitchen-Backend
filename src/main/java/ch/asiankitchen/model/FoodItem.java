package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "food_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Lob
    private String ingredients;

    @Lob
    private String allergies;

    @Size(max = 512)
    @Column(name = "image_url")
    private String imageUrl;
}
