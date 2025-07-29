package ch.asiankitchen.dto;

import ch.asiankitchen.model.FoodItem;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItemDTO {
    private UUID id;

    @NotBlank
    private String name;
    private String description;
    private String ingredients;
    private String allergies;
    private String imageUrl;

    public static FoodItemDTO fromEntity(FoodItem item) {
        return FoodItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .ingredients(item.getIngredients())
                .allergies(item.getAllergies())
                .imageUrl(item.getImageUrl())
                .build();
    }

    public FoodItem toEntity() {
        return FoodItem.builder()
                .id(id)
                .name(name)
                .description(description)
                .ingredients(ingredients)
                .allergies(allergies)
                .imageUrl(imageUrl)
                .build();
    }
}
