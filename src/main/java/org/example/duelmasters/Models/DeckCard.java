package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "decks_cards")
public class DeckCard {

    @EmbeddedId
    private DeckCardId id;

    @ManyToOne
    @MapsId("deckId")
    private Deck deck;

    @ManyToOne
    @MapsId("cardId")
    private Card card;

    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckCard)) return false;
        DeckCard that = (DeckCard) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

