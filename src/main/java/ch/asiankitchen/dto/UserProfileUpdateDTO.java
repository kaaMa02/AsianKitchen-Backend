package ch.asiankitchen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateDTO {
    @NotBlank
    @Size(max = 255)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Valid
    private AddressDTO address;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;   // optional, only if user wants to change it
}
