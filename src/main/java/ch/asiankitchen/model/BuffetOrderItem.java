package ch.asiankitchen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BuffetOrder buffetOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BuffetItem buffetItem;

    @Min(1)
    private int quantity;
}
