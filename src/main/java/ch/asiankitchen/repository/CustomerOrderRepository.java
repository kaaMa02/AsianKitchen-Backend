package ch.asiankitchen.repository;

import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByUserId(UUID userId);
    Optional<CustomerOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
    Optional<CustomerOrder> findByPaymentIntentId(String paymentIntentId);

    @Query("""
        select o
        from CustomerOrder o
        left join fetch o.orderItems oi
        left join fetch oi.menuItem mi
        where o.id = :id
    """)
    Optional<CustomerOrder> findWithItemsAndPrices(UUID id);

    long countByStatusAndPaymentStatus(OrderStatus status, PaymentStatus paymentStatus);

    List<CustomerOrder> findByStatusAndPaymentStatusOrderByCreatedAtDesc(
            OrderStatus status, PaymentStatus paymentStatus
    );
}
