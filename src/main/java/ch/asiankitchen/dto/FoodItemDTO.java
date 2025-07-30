package ch.asiankitchen.dto;

import ch.asiankitchen.model.FoodItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
public class FoodItemDTO {
    private UUID id;

    @NotBlank
    @Size(max=255)
    private String name;

    private String description;
    private String ingredients;
    private String allergies;

    @Size(max=512)
    private String imageUrl;

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

    public static FoodItemDTO fromEntity(FoodItem i) {
        return FoodItemDTO.builder()
                .id(i.getId())
                .name(i.getName())
                .description(i.getDescription())
                .ingredients(i.getIngredients())
                .allergies(i.getAllergies())
                .imageUrl(i.getImageUrl())
                .build();
    }
}
