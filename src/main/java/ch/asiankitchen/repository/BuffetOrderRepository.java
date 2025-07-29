package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuffetOrderRepository extends JpaRepository<BuffetOrder, UUID> {
    List<BuffetOrder> findByUserId(UUID userId);
    List<BuffetOrder> findByStatus(Status status);
    Optional<BuffetOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
}
