package ch.asiankitchen.service;

import ch.asiankitchen.dto.ReservationReadDTO;
import ch.asiankitchen.dto.ReservationWriteDTO;
import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.Status;
import ch.asiankitchen.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationReadDTO createReservation(ReservationWriteDTO dto) {
        Reservation reservation = dto.toEntity();
        Reservation saved = reservationRepository.save(reservation);
        return ReservationReadDTO.fromEntity(saved);
    }

    public List<ReservationReadDTO> getReservationsByUser(UUID userId) {
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(ReservationReadDTO::fromEntity)
                .toList();
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation updateStatus(UUID id, Status status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    public void delete(UUID id) {
        reservationRepository.deleteById(id);
    }
}
