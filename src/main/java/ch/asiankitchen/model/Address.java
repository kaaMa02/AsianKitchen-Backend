package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank
    private String street;

    @NotBlank
    private String streetNo;

    @NotBlank
    private String plz;

    @NotBlank
    private String city;
}
