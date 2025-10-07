package ch.asiankitchen.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class DiscountConfigReadDTO {
    private UUID id;
    private boolean enabled;
    private BigDecimal percentMenu;    // e.g. 20.00
    private BigDecimal percentBuffet;  // e.g. 15.00
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
}