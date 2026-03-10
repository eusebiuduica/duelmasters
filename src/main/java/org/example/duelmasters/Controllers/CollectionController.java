package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.*;
import org.example.duelmasters.Models.User;
import org.example.duelmasters.Services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/collection")
public class CollectionController {

    private final CollectionService collectionService;

    @Autowired
    public CollectionController(CollectionService collectionService) {

        this.collectionService = collectionService;
    }

    @PatchMapping("/sell")
    public ResponseEntity<UserSellResponse> sellCards(@RequestBody @Valid List<CardSellRequest> cards) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = collectionService.sellCards(userId, cards);
        return ResponseEntity.ok(new UserSellResponse(user.getGold()));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CollectionResponse>> getCards() {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(collectionService.findAll(userId));
    }

    @GetMapping("/cards_for_deck")
    public ResponseEntity<List<CardForDeckResponse>> getAllCards() {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(collectionService.findAllCardDetails(userId));
    }

    @GetMapping("/cards_for_sell")
    public ResponseEntity<List<CardSellResponse>> getCardsForSell() {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(collectionService.findAllCardsForSell(userId));
    }
}
