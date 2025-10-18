package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuffetOrderRepository extends JpaRepository<BuffetOrder, UUID> {
    List<BuffetOrder> findByUserId(UUID userId);
    Optional<BuffetOrder> findByPaymentIntentId(String paymentIntentId);
    long countByStatusAndPaymentStatusIn(OrderStatus status, List<PaymentStatus> paymentStatuses);
    @Query("""
      select distinct o
      from BuffetOrder o
      left join fetch o.buffetOrderItems boi
      left join fetch boi.buffetItem bi
      left join fetch bi.foodItem fi
      where o.paymentStatus in :statuses
      order by o.createdAt desc
    """)
    List<BuffetOrder> findAdminVisibleWithItems(@Param("statuses") List<PaymentStatus> statuses);
    List<BuffetOrder> findAllByStatus(OrderStatus status);

}
