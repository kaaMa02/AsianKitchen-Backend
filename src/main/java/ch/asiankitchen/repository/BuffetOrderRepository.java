package ch.asiankitchen.repository;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuffetOrderRepository extends JpaRepository<BuffetOrder, UUID> {
    List<BuffetOrder> findByUserId(UUID userId);
    List<BuffetOrder> findByStatus(OrderStatus status);
    Optional<BuffetOrder> findByIdAndCustomerInfoEmail(UUID id, String email);
    Optional<BuffetOrder> findByPaymentIntentId(String paymentIntentId);

    long countByStatusAndPaymentStatus(OrderStatus status, PaymentStatus paymentStatus);

    // NEW: list only paid NEW, newest first (for Admin page)
    List<BuffetOrder> findByStatusAndPaymentStatusOrderByCreatedAtDesc(
            OrderStatus status, PaymentStatus paymentStatus
    );
}
