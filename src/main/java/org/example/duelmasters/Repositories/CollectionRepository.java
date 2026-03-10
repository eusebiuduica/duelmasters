package org.example.duelmasters.Repositories;

import org.example.duelmasters.DTOs.CardForDeckResponse;
import org.example.duelmasters.DTOs.CardSellResponse;
import org.example.duelmasters.DTOs.CollectionCardQuantity;
import org.example.duelmasters.DTOs.CollectionResponse;
import org.example.duelmasters.Models.Colection;
import org.example.duelmasters.Models.User;
import org.example.duelmasters.Models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CollectionRepository extends JpaRepository<Colection, Integer> {

    List<Colection> findAllByUserId(Integer userId);

    Optional<Colection> findByUserAndCard(User user, Card card);

//    // Repository
//    @Query(value = """
//    SELECT new org.example.duelmasters.DTOs.CollectionResponse(
//        card.id,
//        card.name,
//        col.quantity,
//        col.inPackage,
//        card.image
//    )
//    FROM Colection col
//    JOIN col.card card
//    WHERE col.user.id = :userId
//      AND (:civilization IS NULL OR card.civilization.id = :civilization)
//      AND (:rarity IS NULL OR card.rarity.id = :rarity)
//      AND (:type IS NULL OR card.type.id = :type)
//      AND (:cost IS NULL OR card.cost >= :cost)
//      AND (:power IS NULL OR card.power >= :power)
//      AND (col.inPackage + col.quantity <> 0)
//    ORDER BY
//      CASE WHEN :sortBy = 'mana' AND :sortDir = 'asc' THEN card.cost END ASC,
//      CASE WHEN :sortBy = 'mana' AND :sortDir = 'desc' THEN card.cost END DESC,
//      CASE WHEN :sortBy = 'power' AND :sortDir = 'asc' THEN card.power END ASC,
//      CASE WHEN :sortBy = 'power' AND :sortDir = 'desc' THEN card.power END DESC,
//      card.id ASC
//    """,
//    countQuery = """
//            SELECT count(col)
//            FROM Colection col
//            JOIN col.card card
//            WHERE col.user.id = :userId
//              AND (:civilization IS NULL OR card.civilization.id = :civilization)
//      AND (:rarity IS NULL OR card.rarity.id = :rarity)
//      AND (:type IS NULL OR card.type.id = :type)
//      AND (:cost IS NULL OR card.cost >= :cost)
//      AND (:power IS NULL OR card.power >= :power)
//        """
//    )
//    List<CollectionResponse> findUserCollection(
//            @Param("userId") Integer userId,
//            @Param("civilization") Integer civilization,
//            @Param("rarity") Integer rarity,
//            @Param("type") Integer type,
//            @Param("cost") Integer cost,
//            //@Param("manaMax") Integer manaMax,
//            @Param("power") Integer power,
//            //@Param("powerMax") Integer powerMax,
//            @Param("sortBy") String sortBy,
//            @Param("sortDir") String sortDir
//    );

    @Query(value = """
    SELECT new org.example.duelmasters.DTOs.CardSellResponse(
        card.id,
        card.image,
        col.quantity,
        card.rarity.sellGold
    )
    FROM Colection col
    JOIN col.card card
    WHERE col.user.id = :userId
      AND (col.quantity <> 0)
    ORDER BY
      card.id ASC
    """
    )
    List<CardSellResponse> findUserCollectionToSell(
            @Param("userId") Integer userId
    );

    @Query("""
    SELECT new org.example.duelmasters.DTOs.CardForDeckResponse(
        card.id,
        card.cost,
        card.power,
        card.civilization.id,
        card.image,
        COALESCE(col.quantity, 0) + COALESCE(col.inPackage, 0)
    )
    FROM Colection col
    JOIN col.card card
    WHERE col.user.id = :userId
      AND (COALESCE(col.quantity, 0) + COALESCE(col.inPackage, 0) > 0)
    ORDER BY card.id ASC
    """)
    List<CardForDeckResponse> findAllCardDetails(@Param("userId") Integer userId);

    @Query(value = """
    SELECT new org.example.duelmasters.DTOs.CollectionResponse(
        c.id,
        c.name,
        col.quantity,
        col.inPackage,
        c.image,
        civ.id,
        c.cost,
        t.id,
        r.id,
        c.power,
        r.sellGold
    )
    FROM Card c
    LEFT JOIN Colection col ON col.card.id = c.id AND col.user.id = :userId
    JOIN c.civilization civ
    JOIN c.type t
    JOIN c.rarity r
    ORDER BY
        c.id ASC
    """)
    List<CollectionResponse> findAllCardsForUser(@Param("userId") Integer userId);

    List<Colection> findAllByUserAndCardIn(User user, List<Card> cards);

}