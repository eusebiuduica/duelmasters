package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketplaceOrderRequest {
    @NotNull(message = "Card is required!")
    Integer cardId;

    @NotNull(message = "Quantity is required!")
    @Min(value = 1, message = "At least 1 card should be sold!")
    Integer quantity;

    @NotNull(message = "Price is required!")
    @Min(value = 1, message = "Price must be at least 1!")
    Integer price;
}
