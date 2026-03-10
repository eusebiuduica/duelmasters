package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardSellRequest {
    @NotNull(message = "Invalid card format!")
    private Integer id;

    @Min(value = 1, message = "At least 1 card!")
    private Integer quantity;
}