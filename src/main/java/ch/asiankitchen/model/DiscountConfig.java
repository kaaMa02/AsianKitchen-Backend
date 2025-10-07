package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "discount_config")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DiscountConfig {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "percent_menu", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal percentMenu;

    @Column(name = "percent_buffet", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal percentBuffet;

    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
