package ch.asiankitchen.repository;

import ch.asiankitchen.model.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestaurantInfoRepository extends JpaRepository<RestaurantInfo, UUID> {
}
