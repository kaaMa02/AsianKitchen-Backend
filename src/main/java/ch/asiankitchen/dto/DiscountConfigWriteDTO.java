package ch.asiankitchen.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class DiscountConfigWriteDTO {
    private boolean enabled;
    private BigDecimal percentMenu;
    private BigDecimal percentBuffet;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
}
