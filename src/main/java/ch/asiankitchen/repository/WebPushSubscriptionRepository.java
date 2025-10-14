package ch.asiankitchen.repository;

import ch.asiankitchen.model.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, UUID> {
    List<WebPushSubscription> findByTag(String tag);
    boolean existsByEndpoint(String endpoint);
}
