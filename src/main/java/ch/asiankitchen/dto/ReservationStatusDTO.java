package ch.asiankitchen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationStatusDTO {
    @NotBlank
    private String status;
}