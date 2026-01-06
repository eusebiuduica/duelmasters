package org.example.duelmasters.DTOs;

import jakarta.validation.Valid;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class DeckEditRequest {

    @NotNull(message = "Invalid deck!")
    Integer deckId;

    String name;
    private List<@Valid CardRequest> cards;
}
