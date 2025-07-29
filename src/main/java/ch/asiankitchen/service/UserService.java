package ch.asiankitchen.service;

import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserReadDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserReadDTO::fromEntity)
                .toList();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public UserReadDTO getUserDtoById(UUID id) {
        return userRepository.findById(id)
                .map(UserReadDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public UserReadDTO createUser(UserWriteDTO dto) {
        User user = dto.toEntity();
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return UserReadDTO.fromEntity(savedUser);
    }

    @Transactional
    public User updateUser(UUID id, User updatedUser) {
        User existing = getUserById(id);

        existing.setUsername(updatedUser.getUsername());
        existing.setRole(updatedUser.getRole());
        existing.setFirstName(updatedUser.getFirstName());
        existing.setLastName(updatedUser.getLastName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhoneNumber(updatedUser.getPhoneNumber());
        existing.setAddress(updatedUser.getAddress());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existing);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
