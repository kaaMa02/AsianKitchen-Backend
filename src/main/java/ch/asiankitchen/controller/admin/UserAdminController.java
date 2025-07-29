package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.dto.UserWriteDTO;
import ch.asiankitchen.model.User;
import ch.asiankitchen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public List<UserReadDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserReadDTO getUserById(@PathVariable UUID id) {
        return userService.getUserDtoById(id);
    }

    @PostMapping
    public UserReadDTO createUser(@Valid @RequestBody UserWriteDTO userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/{id}")
    public UserReadDTO updateUser(@PathVariable UUID id, @Valid @RequestBody UserWriteDTO userDto) {
        User existingUser = userService.getUserById(id);

        User updatedUser = userService.updateUser(id, existingUser);

        return UserReadDTO.fromEntity(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
