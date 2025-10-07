package ch.asiankitchen.repository;

import ch.asiankitchen.model.DiscountConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscountConfigRepository extends JpaRepository<DiscountConfig, UUID> { }
