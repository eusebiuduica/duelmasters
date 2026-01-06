package org.example.duelmasters.Repositories;

import org.example.duelmasters.DTOs.DeckResponse;
import org.example.duelmasters.Models.Deck;
import org.example.duelmasters.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Integer> {

    boolean existsByUserAndName(User user, String name);

    Optional<Deck> findByIdAndUserId(Integer id, Integer userId);

    List<Deck> findAllByUserId(Integer userId);

}
