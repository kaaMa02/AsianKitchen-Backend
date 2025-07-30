package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.UserProfileUpdateDTO;
import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.User;
import ch.asiankitchen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public List<UserReadDTO> getAllUsers() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public UserReadDTO getUserById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @PostMapping
    public ResponseEntity<UserReadDTO> createUser(@Valid @RequestBody UserWriteDTO dto) {
        UserReadDTO created = userService.create(dto);
        // build Location: /api/admin/users/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(created);
    }

    @PutMapping("/{id}")
    public UserReadDTO updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        return userService.updateProfile(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }
}
