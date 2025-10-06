package ch.asiankitchen.repository;

import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentMethod;
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
    long countByStatusAndPaymentStatus(OrderStatus status, PaymentStatus paymentStatus);
    long countByStatusAndPaymentMethod(OrderStatus status, PaymentMethod paymentMethod);
    @Query("""
      select distinct o
      from CustomerOrder o
      left join fetch o.orderItems oi
      left join fetch oi.menuItem mi
      left join fetch mi.foodItem fi
      where o.paymentStatus = :status
      order by o.createdAt desc
    """)
    List<CustomerOrder> findPaidWithItems(@Param("status") PaymentStatus status);
    // import org.springframework.data.repository.query.Param;

    @org.springframework.data.jpa.repository.Query("""
      select distinct o
      from CustomerOrder o
      left join fetch o.orderItems oi
      left join fetch oi.menuItem mi
      left join fetch mi.foodItem fi
      where (o.paymentStatus = :succeeded) or (o.paymentMethod = :cash)
      order by o.createdAt desc
    """)
    List<CustomerOrder> findAdminVisibleWithItems(@Param("succeeded") PaymentStatus succeeded,
                                                  @Param("cash") PaymentMethod cash);

}
