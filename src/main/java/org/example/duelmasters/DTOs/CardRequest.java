package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardRequest {

    @NotNull(message = "Invalid card format!")
    private Integer cardId;

    @Min(value = 1, message = "At least 1 card!")
    @Max(value = 4, message = "Maximum 4 card!")
    private Integer quantity;
}
