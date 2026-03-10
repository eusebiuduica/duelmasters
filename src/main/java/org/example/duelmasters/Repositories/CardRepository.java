package org.example.duelmasters.Repositories;

import org.example.duelmasters.Models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {

    List<Card> findAllByBoosterIdAndRarityId(Integer boosterId, Integer rarityId);

    List<Card> findAllByBoosterIdAndRarityIdAndCivilizationId(Integer boosterId, Integer rarityId, Integer civilizationId);

    @Query("""
            SELECT c FROM Card c
            JOIN FETCH c.rarity
            WHERE c.id IN :ids
            """)
    List<Card> findAllByIdWithRarity(Set<Integer> ids);
}
