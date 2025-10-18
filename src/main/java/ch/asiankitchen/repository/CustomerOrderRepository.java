package ch.asiankitchen.repository;

import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByUserId(UUID userId);
    Optional<CustomerOrder> findByPaymentIntentId(String paymentIntentId);
    long countByStatusAndPaymentStatusIn(OrderStatus status, List<PaymentStatus> paymentStatuses);
    @Query("""
      select distinct o
      from CustomerOrder o
      left join fetch o.orderItems oi
      left join fetch oi.menuItem mi
      left join fetch mi.foodItem fi
      where o.paymentStatus in :statuses
      order by o.createdAt desc
    """)
    List<CustomerOrder> findAdminVisibleWithItems(@Param("statuses") List<PaymentStatus> statuses);
    List<CustomerOrder> findAllByStatus(OrderStatus status);
}
