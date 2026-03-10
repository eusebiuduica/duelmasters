package org.example.duelmasters.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class DeckEditRequest {

    @NotNull(message = "Invalid deck!")
    Integer deckId;

    @NotEmpty(message = "Name field must be not empty!")
    String name;

    @NotEmpty
    private List<@Valid CardRequest> cards;
}
