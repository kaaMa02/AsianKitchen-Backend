package ch.asiankitchen.dto;

import ch.asiankitchen.model.Role;
import ch.asiankitchen.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserReadDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;

    public static UserReadDTO fromEntity(User u) {
        return UserReadDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .build();
    }
}
