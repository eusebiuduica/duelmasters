package org.example.duelmasters.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeckCardResponse {
    private Integer cardId;
    private String cardName;
    private Integer quantity;
}
