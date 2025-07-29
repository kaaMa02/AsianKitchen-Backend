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
public class RestaurantInfo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;

    @Lob
    private String aboutText;

    @Embedded
    private Address address;

    private String phone;
    private String email;
    private String instagramUrl;
    private String googleMapsUrl;

    @Lob
    private String openingHours;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
