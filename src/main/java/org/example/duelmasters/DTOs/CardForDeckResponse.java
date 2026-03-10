package org.example.duelmasters.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

// this is used as response where user needs cards for deck, like only these fields
@AllArgsConstructor
@Data
public class CardForDeckResponse {

    Integer cardId;
    Integer cardMana;
    Integer cardPower;
    Integer cardCivilization;
    String cardImage;
    Integer cardQuantity;
}
