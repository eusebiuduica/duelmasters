package org.example.duelmasters.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class DeckResponse {
    Integer deckId;
    String deckName;
    List<DeckCardResponse> deckCards;
}
