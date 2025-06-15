package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuffetOrderRepository extends JpaRepository<BuffetOrder, UUID> {
    List<BuffetOrder> findByUserId(UUID userId);
    Optional<BuffetOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
}
