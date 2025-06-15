package ch.asiankitchen.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @Embedded
    private Address address;
}
