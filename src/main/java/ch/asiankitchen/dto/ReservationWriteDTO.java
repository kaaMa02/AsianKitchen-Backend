package ch.asiankitchen.dto;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.ReservationStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ReservationWriteDTO {
    @NotNull
    private CustomerInfoDTO customerInfo;

    @NotNull
    private LocalDateTime reservationDateTime;

    @Min(1)
    private int numberOfPeople;

    private String specialRequests;

    public Reservation toEntity() {
        return Reservation.builder()
                .customerInfo(CustomerInfoDTO.toEntity(customerInfo))
                .reservationDateTime(reservationDateTime)
                .numberOfPeople(numberOfPeople)
                .specialRequests(specialRequests)
                .status(ReservationStatus.REQUESTED)
                .build();
    }
}
