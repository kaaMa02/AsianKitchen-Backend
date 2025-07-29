package ch.asiankitchen.controller;

import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.Role;
import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import ch.asiankitchen.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserReadDTO registerCustomer(@Valid @RequestBody UserWriteDTO userDto) {
        return authService.registerCustomer(userDto);
    }
}
