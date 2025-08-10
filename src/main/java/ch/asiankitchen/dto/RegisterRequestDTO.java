package ch.asiankitchen.dto;

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
public class RegisterRequestDTO {

    @NotBlank
    @Size(min=3, max=50)
    private String username;

    @NotBlank
    @Size(min=6)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email(message="Invalid email")
    private String email;

    private String phoneNumber;
}
