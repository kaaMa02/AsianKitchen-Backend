package ch.asiankitchen.controller;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.dto.ReservationWriteDTO;
import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.ReservationRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationPublicController {

    private final ReservationRepository reservationRepository;

    public ReservationPublicController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @PostMapping
    public ReservationReadDTO createReservation(@Valid @RequestBody ReservationWriteDTO dto) {
        Reservation reservation = dto.toEntity();
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationReadDTO.fromEntity(savedReservation);
    }

    @GetMapping("/my-reservations")
    public List<ReservationReadDTO> getReservationsByUserId(@RequestParam UUID userId){
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(ReservationReadDTO::fromEntity)
                .toList();
    }
}

