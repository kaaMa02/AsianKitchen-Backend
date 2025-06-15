package ch.asiankitchen.repository;

import ch.asiankitchen.model.MenuItem;
import ch.asiankitchen.model.MenuItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByAvailableTrue();
}
