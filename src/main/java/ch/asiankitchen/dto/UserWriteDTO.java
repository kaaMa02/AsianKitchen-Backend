package ch.asiankitchen.dto;

import ch.asiankitchen.model.Role;
import ch.asiankitchen.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserWriteDTO(
        @NotBlank
        String username,
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,
        Role role
) {
    public User toEntity() {
        return User.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
    }
}