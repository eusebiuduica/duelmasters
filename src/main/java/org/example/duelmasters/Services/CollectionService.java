package org.example.duelmasters.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.duelmasters.DTOs.CardRequest;
import org.example.duelmasters.DTOs.CollectionFilter;
import org.example.duelmasters.DTOs.CollectionResponse;
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

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CollectionRepository collectionRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public User sellCards(Integer userId, List<CardRequest> cardsSell) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // 1️⃣ Aggregate quantities per cardId
        Map<Integer, Integer> aggregated = new HashMap<>();

        for (CardRequest request : cardsSell) {

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantity must be a positive number");
            }

            aggregated.merge(
                    request.getCardId(),
                    request.getQuantity(),
                    Integer::sum
            );
        }

        int goldReceived = 0;

        StringBuilder sbAuditDetails = new StringBuilder("Sold cards: ");

        // 2️⃣ Validate and apply once per card
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {

            Integer cardId = entry.getKey();
            Integer totalQuantity = entry.getValue();

            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Card not found with id " + cardId));

            sbAuditDetails.append(card.getName())
                    .append(" (ID= ").append(card.getId()).append(")").append(", ");

            Colection c = collectionRepository.findByUserAndCard(user, card)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User does not own this card: " + card.getName()));

            if (c.getQuantity() < totalQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough copies of card " + card.getName() + " to sell");
            }

            // Update quantity once
            c.setQuantity(c.getQuantity() - totalQuantity);

            // Calculate gold using rarity sell price
            goldReceived += totalQuantity * card.getRarity().getSellGold();
        }

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

    public List<CollectionResponse> findAll(Integer userId, CollectionFilter filter) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        String sortByDefault = "id";
        String sortDirDefault = "asc";
        String sortByInitial = filter.getSortBy();
        String sortDirInitial = filter.getSortDir();

        String sortByFinal = (sortByInitial != null && Set.of("mana", "power", "cost", "id").contains(sortByInitial))
                ? sortByInitial
                : sortByDefault;

        String sortDirFinal = (sortDirInitial != null && Set.of("asc", "desc").contains(sortDirInitial.toLowerCase()))
                ? sortDirInitial.toLowerCase()
                : sortDirDefault;

        //Pageable pageable = PageRequest.of(100, filter.getPageSize());

        return collectionRepository.findUserCollection(
                userId,
                filter.getCivilization(),
                filter.getRarity(),
                filter.getType(),
                filter.getManaMin(),
                filter.getManaMax(),
                filter.getPowerMin(),
                filter.getPowerMax(),
                sortByFinal,
                sortDirFinal);
    }
}

