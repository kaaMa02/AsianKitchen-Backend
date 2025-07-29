package ch.asiankitchen.dto;

import ch.asiankitchen.model.Address;
import ch.asiankitchen.model.RestaurantInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInfoWriteDTO {

    @NotBlank
    private String name;

    private String aboutText;

    @NotBlank
    private String phone;

    @Email
    private String email;

    private String instagramUrl;
    private String googleMapsUrl;
    private String openingHours;

    private Address address;

    public RestaurantInfo toEntity() {
        return RestaurantInfo.builder()
                .name(name)
                .aboutText(aboutText)
                .phone(phone)
                .email(email)
                .instagramUrl(instagramUrl)
                .googleMapsUrl(googleMapsUrl)
                .openingHours(openingHours)
                .address(address)
                .build();
    }
}
