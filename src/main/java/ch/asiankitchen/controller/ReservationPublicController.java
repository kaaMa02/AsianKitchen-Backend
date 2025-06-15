package ch.asiankitchen.controller;

import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationPublicController {

    private final ReservationRepository reservationRepository;

    public ReservationPublicController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @PostMapping
    public Reservation create(@RequestBody Reservation r) {
        r.setCreatedAt(LocalDateTime.now());
        r.setStatus(Status.RESERVATION_REQUEST_SENT);
        return reservationRepository.save(r);
    }

    @GetMapping("/my-reservations")
    public List<Reservation> mine(@RequestParam UUID userId){
        return reservationRepository.findByUserId(userId);
    }

}

