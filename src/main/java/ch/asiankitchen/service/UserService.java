package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly=true)
    public List<UserReadDTO> listAll() {
        return repo.findAll().stream()
                .map(UserReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(UserReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional
    public UserReadDTO create(UserWriteDTO dto) {
        User entity = dto.toEntity();
        return UserReadDTO.fromEntity(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public UserReadDTO getProfile(UUID userId) {
        return repo.findById(userId)
                .map(UserReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public UserReadDTO updateProfile(UUID userId, UserProfileUpdateDTO dto) {
        var user = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(new ch.asiankitchen.model.Address(
                dto.getAddress().getStreet(),
                dto.getAddress().getStreetNo(),
                dto.getAddress().getPlz(),
                dto.getAddress().getCity()
        ));
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }
        return UserReadDTO.fromEntity(repo.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
