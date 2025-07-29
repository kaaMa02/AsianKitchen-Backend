package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 255)
    private String name;

    @Lob
    private String description;

    @Lob
    private String ingredients;

    @Lob
    private String allergies;

    @Size(max = 512)
    private String imageUrl;
}
