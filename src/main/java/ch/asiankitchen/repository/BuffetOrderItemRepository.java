package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuffetOrderItemRepository extends JpaRepository<BuffetOrderItem, UUID> {
    // Optional enhancement
    List<BuffetOrderItem> findByBuffetOrderId(UUID buffetOrderId);
}
