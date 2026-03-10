package org.example.duelmasters.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.duelmasters.DTOs.*;
import org.example.duelmasters.Models.AuditLog;
import org.example.duelmasters.Models.Card;
import org.example.duelmasters.Models.Colection;
import org.example.duelmasters.Models.User;
import org.example.duelmasters.Repositories.AuditLogRepository;
import org.example.duelmasters.Repositories.CardRepository;
import org.example.duelmasters.Repositories.CollectionRepository;
import org.example.duelmasters.Repositories.UserRepository;
import org.example.duelmasters.Utils.AuditAction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CollectionRepository collectionRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public User sellCards(Integer userId, List<CardSellRequest> cardsSell) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // 1️⃣ Aggregate quantities per cardId
        Map<Integer, Integer> aggregated = new HashMap<>();

        for (CardSellRequest request : cardsSell) {

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantity must be a positive number");
            }

            aggregated.merge(
                    request.getId(),
                    request.getQuantity(),
                    Integer::sum
            );
        }

        int goldReceived = 0;

        StringBuilder sbAuditDetails = new StringBuilder("Sold cards: ");

        Set<Integer> cardIds = aggregated.keySet();

        // we get the cards ids and rarities from DB
        List<Card> cards = cardRepository.findAllByIdWithRarity(cardIds);

        // we get what cards have the user
        List<Colection> collectionQuantities = collectionRepository.findAllByUserAndCardIn(user, cards);

        // we store these details into maps to access them fast
        Map<Integer, Card> cardMap = cards.stream()
                .collect(Collectors.toMap(Card::getId, c -> c));
        Map<Integer, Colection> collectionMap = collectionQuantities.stream()
                .collect(Collectors.toMap(c -> c.getCard().getId(), c -> c));

        // 2️⃣ Validate and apply once per card
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Integer cardId = entry.getKey();
            Integer totalQuantity = entry.getValue();

            Card card = cardMap.get(cardId);
            if (card == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + cardId);
            }

            Colection collectionCard = collectionMap.get(cardId);
            if (collectionCard == null || collectionCard.getQuantity() < totalQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "You do not have enough copies of card: " + card.getName()
                );
            }

            sbAuditDetails.append(card.getName()).append(" x ").append(totalQuantity).append(", ");

            // we subtract
            collectionCard.setQuantity(collectionCard.getQuantity() - totalQuantity);

            // Gold received
            goldReceived += totalQuantity * card.getRarity().getSellGold();
        }

        // here delete the end of audit card, that comma and space
        if (sbAuditDetails.length() > 2) {
            sbAuditDetails.delete(sbAuditDetails.length() - 2, sbAuditDetails.length());
        }

        sbAuditDetails.append("; Gold received: ")
                .append(goldReceived);

        // 3️⃣ Update user gold
        user.setGold(user.getGold() + goldReceived);

        AuditLog auditLog = new AuditLog();
        auditLog.setActorUser(user);
        auditLog.setActorUsername(user.getUsername());
        auditLog.setAction(AuditAction.CARD_SELL);
        auditLog.setDetails(sbAuditDetails.toString());
        auditLogRepository.save(auditLog);

        return user;
    }

    public List<CollectionResponse> findAll(Integer userId) {
        // check user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        List<CollectionResponse> responses = collectionRepository.findAllCardsForUser(userId);
//        // 2️⃣ get all user cards
//        List<Colection> userCards = collectionRepository.findAllByUserId(userId);
//        Map<Integer, Colection> userCardMap = userCards.stream()
//                .collect(Collectors.toMap(c -> c.getCard().getId(), c -> c));
//
//        // 3️⃣ merge into responses
//        List<CollectionResponse> responses = new ArrayList<>();
//        for (Card card : allCards) {
//            Colection col = userCardMap.get(card.getId());
//            int quantity = col != null ? col.getQuantity() : 0;
//            int inPackage = col != null ? col.getInPackage() : 0;
//
//            // optional: skip cards user never got
//            // if(quantity + inPackage == 0) continue;
//
//            CollectionResponse colResp = new CollectionResponse(
//                    card.getId(),
//                    card.getName(),
//                    quantity,
//                    inPackage,
//                    card.getImage(),
//                    card.getCivilization().getId(),
//                    card.getCost(),
//                    card.getType().getId(),
//                    card.getRarity().getId(),
//                    card.getPower(),
//                    card.getRarity().getSellGold()
//            );
//            responses.add(colResp);
//        }

        return responses;

        // here it was db filter - we can use for later
//        String sortByDefault = "id";
//        String sortDirDefault = "asc";
//        String sortByInitial = filter.getSortBy();
//        String sortDirInitial = filter.getSortDir();
//
//        String sortByFinal = (sortByInitial != null && Set.of("mana", "power", "cost", "id").contains(sortByInitial))
//                ? sortByInitial
//                : sortByDefault;
//
//        String sortDirFinal = (sortDirInitial != null && Set.of("asc", "desc").contains(sortDirInitial.toLowerCase()))
//                ? sortDirInitial.toLowerCase()
//                : sortDirDefault;
//
//        //Pageable pageable = PageRequest.of(100, filter.getPageSize());
//
//        return collectionRepository.findUserCollection(
//                userId,
//                filter.getCivilization(),
//                filter.getRarity(),
//                filter.getType(),
//                filter.getCost(),
//                //filter.getManaMax(),
//                filter.getPower(),
//                //filter.getPowerMax(),
//                sortByFinal,
//                sortDirFinal);
    }

    public List<CardSellResponse> findAllCardsForSell(int userId)
    {

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return collectionRepository.findUserCollectionToSell(userId);
    }

    public List<CardForDeckResponse> findAllCardDetails(int userId)
    {

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return collectionRepository.findAllCardDetails(userId);
    }
}

