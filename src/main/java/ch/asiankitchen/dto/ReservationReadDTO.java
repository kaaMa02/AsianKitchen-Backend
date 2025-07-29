package ch.asiankitchen.dto;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationReadDTO(
        UUID id,
        LocalDateTime reservationDateTime,
        int numberOfPeople,
        String specialRequests,
        Status status,
        LocalDateTime createdAt
) {
    public static ReservationReadDTO fromEntity(Reservation r) {
        return new ReservationReadDTO(
                r.getId(),
                r.getReservationDateTime(),
                r.getNumberOfPeople(),
                r.getSpecialRequests(),
                r.getStatus(),
                r.getCreatedAt()
        );
    }
}
