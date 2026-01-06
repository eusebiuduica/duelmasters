package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.*;
import org.example.duelmasters.Models.Card;
import org.example.duelmasters.Models.Colection;
import org.example.duelmasters.Models.User;
import org.example.duelmasters.Repositories.CardRepository;
import org.example.duelmasters.Repositories.CollectionRepository;
import org.example.duelmasters.Repositories.UserRepository;
import org.example.duelmasters.Services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/collection")
public class CollectionController {

    private final CollectionService collectionService;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Autowired
    public CollectionController(CollectionService collectionService,  CollectionRepository collectionRepository, UserRepository userRepository, CardRepository cardRepository) {

        this.collectionService = collectionService;
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    @PatchMapping("/sell")
    public ResponseEntity<UserSellResponse> sellCards(
            @RequestBody @Valid List<CardRequest> cards) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = collectionService.sellCards(userId, cards);
        return ResponseEntity.ok(new UserSellResponse(user.getGold()));
    }


    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public String getCollectionHtml(@RequestBody @Valid CollectionFilter filter) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return "<html><body><p>User not found</p></body></html>";
        }

        List<CollectionResponse> collection = collectionService.findAll(userId, filter);

        String baseUrl = "http://localhost:8080";

        StringBuilder sb = new StringBuilder(
                "<html><body><div style='display:grid;grid-template-columns:repeat(5,1fr);gap:10px;'>"
        );

        for (CollectionResponse col : collection) {
            int totalCopies = col.getQuantity() + col.getInPackage();
            if (totalCopies == 0)
                continue;

            Optional<Card> card = cardRepository.findById(col.getId());
            Optional<Colection> col1 = collectionRepository.findByUserAndCard(user.get(), card.get());
            if (col1.isPresent()) {
                Colection c = col1.get();
                sb.append("<div style='text-align:center;'>")
                        .append("<img src='").append(baseUrl)
                        .append("/").append(c.getCard().getImage())
                        .append("' style='width:100px;height:150px;'/><br/>")
                        .append(c.getCard().getName()).append("<br/>")
                        .append("in collection ").append(c.getQuantity())
                        .append("<br/>in decks ").append(c.getInPackage())
                        .append("</div>");
            }
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CollectionResponse>> getCards(@RequestBody @Valid CollectionFilter filter) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(collectionService.findAll(userId, filter));
    }

}
