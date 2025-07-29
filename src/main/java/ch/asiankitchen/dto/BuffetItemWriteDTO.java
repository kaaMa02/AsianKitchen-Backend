package ch.asiankitchen.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetItemWriteDTO {
    private UUID foodItemId;
    private boolean available;
}
