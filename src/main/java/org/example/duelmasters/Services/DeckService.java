package org.example.duelmasters.Services;

import lombok.RequiredArgsConstructor;
import org.example.duelmasters.DTOs.*;
import org.example.duelmasters.Models.*;
import org.example.duelmasters.Repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final CardRepository cardRepository;
    private final DeckCardRepository deckCardRepository;

    @Transactional
    public Integer createDeck(Integer userId, DeckRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (user.getCurrentNbDecks().equals(user.getMaxNbDecks()))
        {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You reached the maximum number of decks!");
        }

        // 1️⃣ Check deck name uniqueness
        if (deckRepository.existsByUserAndName(user, request.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deck name already exists");
        }

        if (request.getCards() == null || request.getCards().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deck must contain cards");
        }

        // 2️⃣ Aggregate cards (cardId → total quantity)
        Map<Integer, Integer> aggregated = new HashMap<>();

        for (CardRequest card : request.getCards()) {

            // Quantity must be positive
            if (card.getQuantity() == null || card.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Card quantity must be a positive number");
            }

            // Aggregate quantities for duplicate card IDs
            aggregated.merge(
                    card.getId(),
                    card.getQuantity(),
                    Integer::sum
            );
        }

        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Integer cardId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            // Card must exist
            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Error in request!"));

            // User must own the card
            Colection colection = collectionRepository.findByUserAndCard(user, card)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "You do not own card: " + card.getName()));

            int totalOwned = colection.getQuantity() + colection.getInPackage();

            // User must have enough copies (quantity + inPackage)
            if (totalOwned < requestedQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough copies of card " + card.getName()
                                + ". Owned: " + totalOwned
                                + ", requested: " + requestedQuantity);
            }

            if (colection.getInPackage() < requestedQuantity) {
                int neededFromQuantity = requestedQuantity - colection.getInPackage();

                // Take the rest from quantity
                colection.setQuantity(colection.getQuantity() - neededFromQuantity);

                // Set new in package
                colection.setInPackage(requestedQuantity);
            }
        }

        // 3️⃣ Validate deck size and per-card limits
        int totalCards = 0;

        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Integer quantity = entry.getValue();

            if (quantity > 4) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A deck can have max 4 copies of the same card!");
            }

            totalCards += quantity;
        }

        if (totalCards != 40) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A deck must have exactly 40 cards. Current: " + totalCards
            );
        }

        // 4️⃣ Create and save deck
        Deck deck = Deck.builder()
                .name(request.getName())
                .user(user)
                .build();

        deckRepository.save(deck);

        // 5️⃣ Save deck cards (example, depends on your model)
        // Here you probably have a DeckCard / DeckCards table
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Card card = cardRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                             "Card not found with id " + entry.getKey()));

            DeckCard deckCard = DeckCard.builder()
                    .id(new DeckCardId(deck.getId(), card.getId()))
                    .deck(deck)
                    .card(card)
                    .quantity(entry.getValue())
                    .build();

            deckCardRepository.save(deckCard);
        }
        user.setCurrentNbDecks(user.getCurrentNbDecks() + 1);
        return deck.getId();
    }

    @Transactional
    public List<DeckCardResponse> deleteDeck(Integer userId, Integer deckId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deck not found"));

        List<DeckCardResponse> response = new ArrayList<>();
        for (DeckCard deckcard : deck.getDeckcards()) {

            Card card = deckcard.getCard();
            int maxOther = deckCardRepository.findMaxUsedInOtherDecks(userId, card.getId(), deckId);
            int currentUsed = deckcard.getQuantity();

            if (currentUsed > maxOther) {
                Colection colection = collectionRepository.findByUserAndCard(user, card)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "You do not own card: " + card.getName()));
                colection.setInPackage(maxOther);
                colection.setQuantity(colection.getQuantity() +  currentUsed -  maxOther);

                DeckCardResponse cardResponse = new DeckCardResponse();
                cardResponse.setId(card.getId());
                cardResponse.setQuantity(maxOther);

                response.add(cardResponse);
            }
        }

        deckRepository.delete(deck);
        user.setCurrentNbDecks(user.getCurrentNbDecks() - 1);
        return response;
    }

    @Transactional
    public void deleteAllDecks(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        deckRepository.deleteAllByUserId(userId);

        for (Colection colection : collectionRepository.findAllByUserId(userId)){
            colection.setQuantity(colection.getQuantity() + colection.getInPackage());
            colection.setInPackage(0);
        }

        user.setCurrentNbDecks(0);
    }

    @Transactional
    public List<DeckCardResponse> editDeck(Integer userId, DeckEditRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // 1. Load deck and validate ownership
        Deck deck = deckRepository.findById(request.getDeckId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Deck not found"));

        if (!deck.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Deck not found");
        }


        if (!deck.getName().equals(request.getName())) {

            boolean nameExists = deckRepository
                    .existsByUserAndName(deck.getUser(), request.getName());

            if (nameExists) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Deck name already exists");
            }
        }
        deck.setName(request.getName());

         Map<Integer, Integer> aggregated = new HashMap<>();
         int totalCards = 0;

        // 3.1 Aggregate card quantities and validate basic rules
        for (CardRequest cr : request.getCards()) {

            if (cr.getQuantity() == null || cr.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Card quantity must be positive");
            }

            aggregated.merge(cr.getId(), cr.getQuantity(), Integer::sum);
        }

        // 3.2 Validate max 4 copies per card and max 40 total cards
        for (Integer qty : aggregated.values()) {
            if (qty > 4) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A card cannot have more than 4 copies in a deck");
            }
            totalCards += qty;
        }

        if (totalCards != 40) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A deck must have 40 cards");
        }

        List<DeckCardResponse> response = new ArrayList<>();

        for (DeckCard deckcard : deck.getDeckcards()) {

            Card card = deckcard.getCard();
            int maxOther = deckCardRepository.findMaxUsedInOtherDecks(userId, card.getId(), request.getDeckId());
            int currentUsed = deckcard.getQuantity();

            if (currentUsed > maxOther) {
                DeckCardResponse cardResponse = new DeckCardResponse();
                Colection colection = collectionRepository.findByUserAndCard(user, card)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User does not own card: " + card.getName()));
                colection.setInPackage(maxOther);
                colection.setQuantity(colection.getQuantity() + currentUsed - maxOther);

                cardResponse.setId(card.getId());
                cardResponse.setQuantity(maxOther);
                response.add(cardResponse);
            }
        }

        // 3.3 Clear existing deck cards
        deck.getDeckcards().clear();

        // 3.4 Add new deck cards and update collection (quantity / inPackage)
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Card card = cardRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Card not found with id " + entry.getKey()));

            Colection col = collectionRepository.findByUserAndCard(deck.getUser(), card)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "User does not own card " + card.getName()));

            int needed = entry.getValue();
            int available = col.getQuantity() + col.getInPackage();

            if (available < needed) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough copies of card " + card.getName());
            }

            // Move cards from quantity to inPackage if needed
            int move = Math.max(0, needed - col.getInPackage());
            col.setQuantity(col.getQuantity() - move);
            col.setInPackage(col.getInPackage() + move);

            // Create deck-card link
            DeckCard dc = DeckCard.builder()
                    .id(new DeckCardId(deck.getId(), card.getId()))
                    .deck(deck)
                    .card(card)
                    .quantity(needed)
                    .build();

            deck.getDeckcards().add(dc);


        }
        return response;
    }

    public List<DeckResponse> getAllUserDecks(Integer userId)
    {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        List<Deck> userDecks = deckRepository.findAllByUserId(userId);

        List<DeckResponse> decksResponse = new ArrayList<>();

        for (Deck deck : userDecks)
        {
            DeckResponse deckResponse = new DeckResponse();
            List<DeckCardResponse> deckCards =  new ArrayList<>();
            deckResponse.setDeckName(deck.getName());
            deckResponse.setDeckId(deck.getId());
            for (DeckCard deckCard : deck.getDeckcards())
            {
                DeckCardResponse card = new DeckCardResponse();
                card.setQuantity(deckCard.getQuantity());
                card.setId(deckCard.getCard().getId());
                deckCards.add(card);
            }
            deckResponse.setDeckCards(deckCards);
            decksResponse.add(deckResponse);
        }

        return decksResponse;
    }

    public List<DeckCardResponse> getDeckCards(Integer deckId, Integer userId)
    {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // 1. Load deck and validate ownership
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Deck not found"));

        if (!deck.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Deck not found");
        }

        List<DeckCardResponse> deckCardResponse = new ArrayList<>();

        for (DeckCard deckcard : deck.getDeckcards()) {

            Card card = deckcard.getCard();
            DeckCardResponse cardResponse = new DeckCardResponse();
            cardResponse.setId(card.getId());
            cardResponse.setQuantity(deckcard.getQuantity());
            deckCardResponse.add(cardResponse);
        }

        return deckCardResponse;
    }
}
