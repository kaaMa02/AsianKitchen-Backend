package ch.asiankitchen.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderTimingPatchDTO {
    private Integer adminExtraMinutes; // >= 0, ASAP only
}
