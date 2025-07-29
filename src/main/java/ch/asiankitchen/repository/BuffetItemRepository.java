package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuffetItemRepository extends JpaRepository<BuffetItem, UUID> {
    // Optional enhancement
    List<BuffetItem> findByAvailableTrue();
}
