package ch.asiankitchen.dto;

import ch.asiankitchen.model.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    @NotBlank
    private String street;

    @NotBlank
    private String streetNo;

    @NotBlank
    private String plz;

    @NotBlank
    private String city;

    public Address toEntity() {
        return Address.builder()
                .street(street)
                .streetNo(streetNo)
                .plz(plz)
                .city(city)
                .build();
    }

    public static AddressDTO fromEntity(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDTO.builder()
                .street(address.getStreet())
                .streetNo(address.getStreetNo())
                .plz(address.getPlz())
                .city(address.getCity())
                .build();
    }
}
