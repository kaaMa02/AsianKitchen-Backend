package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAd userPublicService;

    @GetMapping
    public List<UserReadDTO> getAllUsers() {
        return userPublicService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserReadDTO getUserById(@PathVariable UUID id) {
        return userPublicService.getUserDtoById(id);
    }

    @PostMapping
    public UserReadDTO createUser(@Valid @RequestBody UserWriteDTO userDto) {
        return userPublicService.createUser(userDto);
    }

    @PutMapping("/{id}")
    public UserReadDTO updateUser(@PathVariable UUID id, @Valid @RequestBody UserWriteDTO userDto) {
        User existingUser = userPublicService.getUserById(id);

        User updatedUser = userPublicService.updateUser(id, existingUser);

        return UserReadDTO.fromEntity(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userPublicService.deleteUser(id);
    }
}
