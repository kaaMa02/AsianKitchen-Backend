package ch.asiankitchen.repository;

import ch.asiankitchen.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByUserId(UUID userId);
    Optional<CustomerOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
}
