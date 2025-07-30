package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.*;

@Service
public class ReservationService {
    private final ReservationRepository repo;

    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ReservationReadDTO create(ReservationWriteDTO dto) {
        var saved = repo.save(dto.toEntity());
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
    public ReservationReadDTO updateStatus(UUID id, ch.asiankitchen.model.ReservationStatus status) {
        var r = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
        r.setStatus(status);
        return ReservationReadDTO.fromEntity(repo.save(r));
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
}
