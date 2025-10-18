package ch.asiankitchen.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderTimingReadDTO {
    private boolean asap;
    private LocalDateTime requestedAt;
    private Integer minPrepMinutes;
    private Integer adminExtraMinutes;
    private LocalDateTime committedReadyAt;
    private LocalDateTime autoCancelAt;
    private LocalDateTime seenAt;
    private LocalDateTime escalatedAt;
}
