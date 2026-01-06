package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DeckCardId implements Serializable {

    private Integer deckId;
    private Integer cardId;
}
