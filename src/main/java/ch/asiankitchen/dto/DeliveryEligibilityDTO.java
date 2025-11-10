package ch.asiankitchen.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEligibilityDTO {
    private boolean deliverable;
    private String message;

    private BigDecimal minOrderChf;
    private BigDecimal feeChf;
    private BigDecimal freeThresholdChf;
}
