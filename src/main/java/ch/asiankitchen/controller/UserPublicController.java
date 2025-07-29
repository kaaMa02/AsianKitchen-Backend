package ch.asiankitchen.controller;

import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import ch.asiankitchen.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserPublicController {

    private final UserService userService;

    public UserPublicController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public User getProfile(@RequestParam UUID userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/profile")
    public User updateProfile(@RequestParam UUID userId, @RequestBody User u) {
        User existing = userService.getUserById(userId);

        return userService.updateUser(userId, existing);
    }
}
