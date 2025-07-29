package ch.asiankitchen.dto;

import ch.asiankitchen.model.Address;
import ch.asiankitchen.model.CustomerInfo;
import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ReservationWriteDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @NotBlank String phone,
        @NotBlank String street,
        @NotBlank String streetNo,
        @NotBlank String plz,
        @NotBlank String city,
        @NotNull LocalDateTime reservationDateTime,
        @Min(1) int numberOfPeople,
        String specialRequests
) {
    public Reservation toEntity() {
        Address address = new Address(street, streetNo, plz, city);
        CustomerInfo customerInfo = new CustomerInfo(firstName, lastName, email, phone, address);
        return Reservation.builder()
                .customerInfo(customerInfo)
                .reservationDateTime(reservationDateTime)
                .numberOfPeople(numberOfPeople)
                .specialRequests(specialRequests)
                .status(Status.RESERVATION_REQUEST_SENT)
                .build();
    }
}
