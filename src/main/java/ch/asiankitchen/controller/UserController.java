package ch.asiankitchen.controller;

import ch.asiankitchen.dto.UserProfileUpdateDTO;
import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public UserReadDTO getProfile(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}/profile")
    public UserReadDTO updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        return service.updateProfile(id, dto);
    }
}