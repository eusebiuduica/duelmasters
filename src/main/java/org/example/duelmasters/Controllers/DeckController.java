package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.*;
import org.example.duelmasters.Models.Colection;
import org.example.duelmasters.Models.Deck;
import org.example.duelmasters.Repositories.CollectionRepository;
import org.example.duelmasters.Services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/deck")
public class DeckController {

    private final DeckService deckService;

    @Autowired
    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping("/create")
    public ResponseEntity<DeckResponse> createDeck(@RequestBody @Valid DeckRequest deck) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(deckService.createDeck(userId, deck));
    }

    @DeleteMapping("/delete")
    public void deleteDeck(@RequestBody Integer deckId) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        deckService.deleteDeck(userId, deckId);
    }

    @PatchMapping("/edit")
    public void editDeck(@RequestBody @Valid DeckEditRequest deck) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        deckService.editDeck(userId, deck);
    }

    @GetMapping("/get_all")
    public ResponseEntity<List<DeckResponse>> getAllUserDecks() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(deckService.getAllUserDecks(userId));
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<List<DeckCardResponse>> getDeck(@PathVariable Integer deckId) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(deckService.getDeckCards(deckId, userId));
    }
}
