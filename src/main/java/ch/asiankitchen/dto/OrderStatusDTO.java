package ch.asiankitchen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusDTO {
    @NotBlank
    private String status;
}
