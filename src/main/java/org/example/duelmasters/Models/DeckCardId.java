package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DeckCardId implements Serializable {

    private Integer deckId;
    private Integer cardId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckCardId)) return false;
        DeckCardId that = (DeckCardId) o;
        return Objects.equals(deckId, that.deckId) &&
                Objects.equals(cardId, that.cardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deckId, cardId);
    }
}
