package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInfo {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "about_text")
    private String aboutText;

    @Embedded
    private Address address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "google_maps_url")
    private String googleMapsUrl;

    @Lob
    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}