package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuffetItemRepository extends JpaRepository<BuffetItem, UUID> {
    List<BuffetItem> findByAvailableTrue();
}
