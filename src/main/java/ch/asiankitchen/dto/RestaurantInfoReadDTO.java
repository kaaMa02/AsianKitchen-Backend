package ch.asiankitchen.dto;

import ch.asiankitchen.model.Address;
import ch.asiankitchen.model.RestaurantInfo;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInfoReadDTO {
    private UUID id;
    private String name;
    private String aboutText;
    private String phone;
    private String email;
    private String instagramUrl;
    private String googleMapsUrl;
    private String openingHours;
    private Address address;

    public static RestaurantInfoReadDTO fromEntity(RestaurantInfo info) {
        return RestaurantInfoReadDTO.builder()
                .id(info.getId())
                .name(info.getName())
                .aboutText(info.getAboutText())
                .phone(info.getPhone())
                .email(info.getEmail())
                .instagramUrl(info.getInstagramUrl())
                .googleMapsUrl(info.getGoogleMapsUrl())
                .openingHours(info.getOpeningHours())
                .address(info.getAddress())
                .build();
    }
}
