package ch.asiankitchen.dto;

import ch.asiankitchen.model.Role;
import ch.asiankitchen.model.User;

import java.util.UUID;

public record UserReadDTO(UUID id, String username, String email, Role role) {

    public static UserReadDTO fromEntity(User user) {
        return new UserReadDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}