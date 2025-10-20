package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    private CustomerInfo customerInfo;

    @Column(name = "reservation_date_time", nullable = false)
    private LocalDateTime reservationDateTime;

    @Column(name = "number_of_people", nullable = false)
    private int numberOfPeople;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "auto_cancel_at")
    private LocalDateTime autoCancelAt;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
