package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.Reservation;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.*;

@Service
public class ReservationService {
    private final ReservationRepository repo;
    private final ReservationEmailService emailService;

    @Value("${app.timezone:Europe/Zurich}")
    private String appTz;

    private LocalDateTime toUtc(LocalDateTime local) {
        if (local == null) return null;
        return local.atZone(ZoneId.of(appTz))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public ReservationService(ReservationRepository repo, ReservationEmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    @Transactional
    public ReservationReadDTO create(ReservationWriteDTO dto) {
        var entity = dto.toEntity();
        // 🔧 NEW: the date picker is local CH time — store as UTC
        entity.setReservationDateTime(toUtc(dto.getReservationDateTime()));
        var saved = repo.save(entity);
        emailService.sendNewReservationToRestaurant(saved);
        return ReservationReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ReservationReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(ReservationReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Transactional(readOnly = true)
    public List<ReservationReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(ReservationReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationReadDTO updateStatus(UUID id, ReservationStatus newStatus) {
        Reservation res = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        res.setStatus(newStatus);

        if (newStatus == ReservationStatus.CONFIRMED) {
            emailService.sendConfirmationToCustomer(res);
        } else if (newStatus == ReservationStatus.CANCELLED || newStatus == ReservationStatus.REJECTED) {
            emailService.sendRejectionToCustomer(res);
        }

        return ReservationReadDTO.fromEntity(res);
    }

    @Transactional(readOnly = true)
    public List<ReservationReadDTO> listAll() {
        return repo.findAll().stream()
                .map(ReservationReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ReservationReadDTO track(UUID id, String email) {
        return repo.findByIdAndCustomerInfoEmail(id, email)
                .map(ReservationReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Transactional(readOnly = true)
    public List<ReservationReadDTO> listByStatus(ReservationStatus status) {
        return repo.findAllByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(ReservationReadDTO::fromEntity)
                .toList();
    }
}
