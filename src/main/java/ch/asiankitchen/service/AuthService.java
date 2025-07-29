package ch.asiankitchen.service;

import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.Role;
import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserReadDTO registerCustomer(UserWriteDTO userDto) {
        return getUserReadDTO(userDto, userRepository, passwordEncoder);
    }

    public static UserReadDTO getUserReadDTO(UserWriteDTO userDto, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository.findByUsername(userDto.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        User newUser = userDto.toEntity();
        newUser.setPassword(passwordEncoder.encode(userDto.password()));
        newUser.setRole(Role.CUSTOMER); // All registrants are customers

        User savedUser = userRepository.save(newUser);
        return UserReadDTO.fromEntity(savedUser);
    }
}
