package org.example.duelmasters.Repositories;

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

@Repository
public interface CollectionRepository extends JpaRepository<Colection, Integer> {

    Optional<Colection> findByUserAndCard(User user, Card card);

    List<Colection> findByUserOrderByCard_IdAsc(User user);

    // Repository
    @Query(value = """
    SELECT new org.example.duelmasters.DTOs.CollectionResponse(
        card.id,
        card.name,
        col.quantity,
        col.inPackage
    )
    FROM Colection col
    JOIN col.card card
    WHERE col.user.id = :userId
      AND (:civilization IS NULL OR card.civilization.id = :civilization)
      AND (:rarity IS NULL OR card.rarity.id = :rarity)
      AND (:type IS NULL OR card.type.id = :type)
      AND (:manaMin IS NULL OR card.cost >= :manaMin)
      AND (:manaMax IS NULL OR card.cost <= :manaMax)
      AND (:powerMin IS NULL OR card.power >= :powerMin)
      AND (:powerMax IS NULL OR card.power <= :powerMax)
      AND (col.inPackage + col.quantity <> 0)
    ORDER BY
      CASE WHEN :sortBy = 'mana' AND :sortDir = 'asc' THEN card.cost END ASC,
      CASE WHEN :sortBy = 'mana' AND :sortDir = 'desc' THEN card.cost END DESC,
      CASE WHEN :sortBy = 'power' AND :sortDir = 'asc' THEN card.power END ASC,
      CASE WHEN :sortBy = 'power' AND :sortDir = 'desc' THEN card.power END DESC,
      card.id ASC
    """,
    countQuery = """
            SELECT count(col)
            FROM Colection col
            JOIN col.card card
            WHERE col.user.id = :userId
              AND (:civilization IS NULL OR card.civilization.id = :civilization)
      AND (:rarity IS NULL OR card.rarity.id = :rarity)
      AND (:type IS NULL OR card.type.id = :type)
      AND (:manaMin IS NULL OR card.cost >= :manaMin)
      AND (:manaMax IS NULL OR card.cost <= :manaMax)
      AND (:powerMin IS NULL OR card.power >= :powerMin)
      AND (:powerMax IS NULL OR card.power <= :powerMax)
        """
    )
    List<CollectionResponse> findUserCollection(
            @Param("userId") Integer userId,
            @Param("civilization") Integer civilization,
            @Param("rarity") Integer rarity,
            @Param("type") Integer type,
            @Param("manaMin") Integer manaMin,
            @Param("manaMax") Integer manaMax,
            @Param("powerMin") Integer powerMin,
            @Param("powerMax") Integer powerMax,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir
    );



}