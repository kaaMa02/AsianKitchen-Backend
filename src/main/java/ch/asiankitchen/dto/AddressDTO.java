package ch.asiankitchen.dto;

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
}
