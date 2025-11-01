package ch.asiankitchen.dto;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReservationReadDTO {
    private UUID id;
    private CustomerInfoDTO customerInfo;
    private LocalDateTime reservationDateTime;
    private int numberOfPeople;
    private String specialRequests;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    private LocalDateTime seenAt;
    private LocalDateTime escalatedAt;

    public static ReservationReadDTO fromEntity(Reservation r) {
        return ReservationReadDTO.builder()
                .id(r.getId())
                .customerInfo(CustomerInfoDTO.fromEntity(r.getCustomerInfo()))
                .reservationDateTime(r.getReservationDateTime())
                .numberOfPeople(r.getNumberOfPeople())
                .specialRequests(r.getSpecialRequests())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .seenAt(r.getSeenAt())
                .escalatedAt(r.getEscalatedAt())
                .build();
    }
}
