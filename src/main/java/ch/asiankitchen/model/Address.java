package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String street;
    private String streetNo;
    private String plz;
    private String city;
}
