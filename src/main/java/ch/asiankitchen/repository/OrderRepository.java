package ch.asiankitchen.repository;

import ch.asiankitchen.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByUserId(UUID userId);
    Optional<CustomerOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
}
