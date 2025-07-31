package ch.asiankitchen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BuffetItemWriteDTO {
    @NotNull
    private UUID foodItemId;

    private boolean available;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;
}