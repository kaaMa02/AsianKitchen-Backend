package ch.asiankitchen.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuffetItemReadDTO {
    private UUID id;
    private boolean available;
    private UUID foodItemId;
    private String foodItemName;
}
