package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuffetOrderItemRepository extends JpaRepository<BuffetOrderItem, UUID> {
}
