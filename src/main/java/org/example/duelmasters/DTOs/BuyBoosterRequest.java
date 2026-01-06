package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyBoosterRequest {

    @NotNull(message = "Invalid booster format!")
    @Min(value = 1, message = "Invalid booster format!")
    private Integer boosterId;

    // here there may be used some additional fields
}
