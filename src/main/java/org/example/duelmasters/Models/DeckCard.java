package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "deck_id")
    private Deck deck;

    @ManyToOne
    @MapsId("cardId")
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(nullable = false)
    private Integer quantity;
}
