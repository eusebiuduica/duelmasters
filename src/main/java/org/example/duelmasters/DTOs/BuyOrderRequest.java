package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class BuyOrderRequest {
    @NotNull(message = "Invalid format!")
    private Integer orderId;

    @NotNull(message = "Invalid format!")
    @Min(value = 1, message = "At least 1 card should be bought!")
    private Integer quantity;
}
