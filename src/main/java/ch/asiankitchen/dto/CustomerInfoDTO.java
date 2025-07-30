package ch.asiankitchen.dto;

import ch.asiankitchen.model.CustomerInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoDTO {
    @NotBlank
    @Size(max=255)
    private String firstName;

    @NotBlank
    @Size(max=255)
    private String lastName;

    @NotBlank @Email
    @Size(max=255)
    private String email;

    @NotBlank
    @Size(max=50)
    private String phone;

    @Valid
    private AddressDTO address;

    public static CustomerInfo toEntity(CustomerInfoDTO dto) {
        return ch.asiankitchen.model.CustomerInfo.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(new ch.asiankitchen.model.Address(
                        dto.getAddress().getStreet(),
                        dto.getAddress().getStreetNo(),
                        dto.getAddress().getPlz(),
                        dto.getAddress().getCity()
                ))
                .build();
    }

    public static CustomerInfoDTO fromEntity(CustomerInfo e) {
        return CustomerInfoDTO.builder()
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(AddressDTO.builder()
                        .street(e.getAddress().getStreet())
                        .streetNo(e.getAddress().getStreetNo())
                        .plz(e.getAddress().getPlz())
                        .city(e.getAddress().getCity())
                        .build())
                .build();
    }
}