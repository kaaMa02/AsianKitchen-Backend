package ch.asiankitchen.repository;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByUserId(UUID userId);
    Optional<Reservation> findByIdAndCustomerInfoEmail(UUID id, String email);
    List<Reservation> findAllByStatusOrderByCreatedAtDesc(ReservationStatus status);
}
