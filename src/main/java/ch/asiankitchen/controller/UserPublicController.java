package ch.asiankitchen.controller;

import ch.asiankitchen.model.User;
import ch.asiankitchen.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserPublicController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserPublicController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public User profile(@RequestParam UUID userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    @PutMapping("/profile")
    public User updateProfile(@RequestParam UUID userId, @RequestBody User u) {
        User existing = userRepository.findById(userId).orElseThrow();
        existing.setFirstName(u.getFirstName());
        existing.setLastName(u.getLastName());
        existing.setEmail(u.getEmail());
        existing.setPhoneNumber(u.getPhoneNumber());
        existing.setAddress(u.getAddress());
        if (u.getPassword()!=null && !u.getPassword().isEmpty()){
            existing.setPassword(passwordEncoder.encode(u.getPassword()));
        }
        return userRepository.save(existing);
    }
}
