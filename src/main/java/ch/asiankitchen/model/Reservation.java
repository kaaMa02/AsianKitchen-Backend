package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Embedded
    private CustomerInfo customerInfo;

    private LocalDateTime reservationDateTime;
    private int numberOfPeople;

    @Lob
    private String specialRequests;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
