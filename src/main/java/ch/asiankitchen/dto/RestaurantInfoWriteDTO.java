package ch.asiankitchen.dto;

import ch.asiankitchen.model.Address;
import ch.asiankitchen.model.RestaurantInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
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

    @Valid
    private AddressDTO address;

    public RestaurantInfo toEntity() {
        return RestaurantInfo.builder()
                .name(name)
                .aboutText(aboutText)
                .phone(phone)
                .email(email)
                .instagramUrl(instagramUrl)
                .googleMapsUrl(googleMapsUrl)
                .openingHours(openingHours)
                .address(new Address(
                        address.getStreet(),
                        address.getStreetNo(),
                        address.getPlz(),
                        address.getCity()))
                .build();
    }
}
