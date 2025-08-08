package ch.asiankitchen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageRequestDTO {
    @NotBlank @Size(max = 255)
    private String name;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @NotBlank @Size(max = 4000)
    private String message;
}
