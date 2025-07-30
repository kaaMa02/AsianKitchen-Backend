package ch.asiankitchen.dto;

import ch.asiankitchen.model.RestaurantInfo;
import lombok.*;

import java.util.UUID;

@Data
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
    private AddressDTO address;

    public static RestaurantInfoReadDTO fromEntity(RestaurantInfo i) {
        var addr = i.getAddress();
        return RestaurantInfoReadDTO.builder()
                .id(i.getId())
                .name(i.getName())
                .aboutText(i.getAboutText())
                .phone(i.getPhone())
                .email(i.getEmail())
                .instagramUrl(i.getInstagramUrl())
                .googleMapsUrl(i.getGoogleMapsUrl())
                .openingHours(i.getOpeningHours())
                .address(AddressDTO.builder()
                        .street(addr.getStreet())
                        .streetNo(addr.getStreetNo())
                        .plz(addr.getPlz())
                        .city(addr.getCity())
                        .build())
                .build();
    }
}
