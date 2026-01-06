package org.example.duelmasters.Repositories;

import org.example.duelmasters.Models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {

    @Query(
            value = """
            SELECT *
            FROM cards
            WHERE booster = ?1
            ORDER BY RAND()
            """,
            nativeQuery = true
    )
    List<Card> findRandomByBooster(
            Integer boosterId,
            Pageable pageable
    );

    List<Card> findAllByBoosterIdAndRarityId(Integer boosterId, Integer rarityId);
    List<Card> findAllByBoosterIdAndRarityIdAndCivilizationId(Integer boosterId, Integer rarityId, Integer civilizationId);
}
