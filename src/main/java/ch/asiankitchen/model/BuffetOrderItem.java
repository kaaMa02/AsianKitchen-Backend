package ch.asiankitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetOrderItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne
    private BuffetOrder buffetOrder;

    @ManyToOne
    private BuffetItem buffetItem;

    private int quantity;
}
