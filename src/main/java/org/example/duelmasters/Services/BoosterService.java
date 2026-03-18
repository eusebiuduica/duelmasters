package org.example.duelmasters.Services;

import jakarta.transaction.Transactional;
import org.example.duelmasters.DTOs.Booster.BoosterResponse;
import org.example.duelmasters.DTOs.Booster.BuyBoosterRequest;
import org.example.duelmasters.DTOs.Booster.BuyBoosterResponse;
import org.example.duelmasters.DTOs.CardResponse;
import org.example.duelmasters.Infrastructure.AllSseManager;
import org.example.duelmasters.Models.*;
import org.example.duelmasters.Repositories.*;
import org.example.duelmasters.Utils.AuditAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class BoosterService {

    private final int commonChance;
    private final CardRepository cardRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final BoosterRepository boosterRepository;
    private final AuditLogRepository auditLogRepository;
    private final AllSseManager sseManager;

    public BoosterService(@Value("${chance.common}") int commonChance,
                          CardRepository cardRepository,
                          CollectionRepository collectionRepository,
                          UserRepository userRepository,
                          BoosterRepository boosterRepository,
                          AuditLogRepository auditLogRepository,
                          AllSseManager sseManager) {
        this.commonChance = commonChance;
        this.cardRepository = cardRepository;
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.boosterRepository = boosterRepository;
        this.auditLogRepository = auditLogRepository;
        this.sseManager = sseManager;
    }

    @Transactional
    public BuyBoosterResponse buyBooster(BuyBoosterRequest request, Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!"));

        Integer boosterId =  request.getBoosterId();
        Booster booster = boosterRepository.findById(boosterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booster not found!"));

        if (booster.getQuantity() < 1){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There are no more boosters to buy!"
            );
        }

        if (user.getGold() < booster.getPrice()) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Not enough gold to buy this booster!"
            );
        }

        user.setGold(user.getGold() - booster.getPrice());

        List<Card> boosterCards = openBooster(boosterId);
        List<CardResponse> cardResponses = new ArrayList<>();

        for (Card card : boosterCards) {
            Optional<Colection> existing = collectionRepository.findByUserAndCard(user, card);
            CardResponse b_card = new CardResponse(card.getId());

            if (existing.isPresent()) {
                Colection c = existing.get();
                c.setQuantity(c.getQuantity() + 1);
            } else {
                Colection c = Colection.builder()
                        .user(user)
                        .card(card)
                        .quantity(1)
                        .inPackage(0)
                        .build();
                collectionRepository.save(c);
                //b_card.setName(card.getName() + " NEW");
            }

            cardResponses.add(b_card);
        }

        String cardList = boosterCards.stream()
                .map(c -> c.getName() + " (ID= " + c.getId() + ")")
                .collect(Collectors.joining(", "));

        String details = "Bought cards: " + cardList +
                "; Booster: " + booster.getName() +
                "; Price: " + booster.getPrice() + " gold";

        AuditLog auditLog = new AuditLog();
        auditLog.setActorUser(user);
        auditLog.setActorUsername(user.getUsername());
        auditLog.setAction(AuditAction.BOOSTER_BUY);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);

        BuyBoosterResponse response = new BuyBoosterResponse();
        response.setCards(cardResponses);
        response.setGoldLeft(user.getGold());

        booster.setQuantity(booster.getQuantity() - 1);
        BoosterResponse  boosterResponse = new BoosterResponse();
        boosterResponse.setQuantity(booster.getQuantity());
        boosterResponse.setId(boosterId - 1);
        sseManager.broadcast(boosterResponse, "BOOSTER_BOUGHT");
        return response;
    }

    // helper function for random selection
    private Card popRandom(List<Card> cards) {

        int idx = ThreadLocalRandom.current().nextInt(0, cards.size());
        return cards.remove(idx);
    }

    public List<Card> openBooster(Integer boosterId) {

        // generate booster
        List<Card> booster = new ArrayList<>();

        List<Card> commons = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityId(boosterId, 1));
        List<Card> uncommons = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityId(boosterId, 2));
        List<Card> rares = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityId(boosterId, 3));
        List<Card> veryRares = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityId(boosterId, 4));
        List<Card> superRares = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityId(boosterId, 5));

        // 2 commons are guaranteed
        // 1 more common - depends on commonChance
        booster.add(popRandom(commons));
        booster.add(popRandom(commons));
        int roll = ThreadLocalRandom.current().nextInt(1, 1000);
        if (roll < commonChance) {
            booster.add(popRandom(commons));
        }

        // 2 uncommon are guaranteed
        booster.add(popRandom(uncommons));
        booster.add(popRandom(uncommons));

        // 1 rare is guaranteed
        booster.add(popRandom(rares));

        // 1 very rare or super rare if the 3rd common is not
        if (roll >= commonChance) {
            boolean superRare = ThreadLocalRandom.current().nextBoolean();
            booster.add(superRare && !superRares.isEmpty() ? popRandom(superRares) : popRandom(veryRares));
        }

        return booster;
    }

    public List<BoosterResponse> getAllBoosters() {

        return boosterRepository.findAll()
                .stream()
                .map(booster -> {
                    BoosterResponse dto = new BoosterResponse();
                    dto.setId(booster.getId());
                    dto.setName(booster.getName());
                    dto.setPrice(booster.getPrice());
                    dto.setImage(booster.getImage());
                    dto.setQuantity(booster.getQuantity());
                    return dto;
                })
                .toList();
    }

    public List<Booster> resetAllQuantities() {
        List<Booster> boosters = boosterRepository.findAll();

        for (Booster b : boosters) {
            b.setQuantity(10);
        }

        boosterRepository.saveAll(boosters); // save updated quantities

        return boosters; // return list to frontend or log
    }
}
