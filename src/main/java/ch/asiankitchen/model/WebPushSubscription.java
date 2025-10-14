package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "web_push_subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_webpush_endpoint", columnNames = {"endpoint"})
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WebPushSubscription {
    @Id @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String endpoint;

    @Column(nullable = false, length = 200)
    private String p256dh;

    @Column(nullable = false, length = 200)
    private String auth;

    /** Optional classification: "admin" (tablet), "owner", etc. */
    @Column(length = 50)
    private String tag;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void pre() { this.createdAt = LocalDateTime.now(); }
}
