package org.example.duelmasters.Repositories;


import org.example.duelmasters.Models.Deck;
import org.example.duelmasters.Models.DeckCard;
import org.example.duelmasters.Models.DeckCardId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeckCardRepository extends JpaRepository<DeckCard, DeckCardId> {

    @Query("""
    SELECT COALESCE(MAX(dc.quantity), 0) 
    FROM DeckCard dc
    JOIN dc.deck d
    WHERE d.user.id = :userId
      AND dc.card.id = :cardId
      AND d.id <> :currentDeckId
""")
    Integer findMaxUsedInOtherDecks(@Param("userId") Integer userId,
                                    @Param("cardId") Integer cardId,
                                    @Param("currentDeckId") Integer deckId);

}
