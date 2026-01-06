package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.BuyBoosterRequest;
import org.example.duelmasters.DTOs.CardResponse;
import org.example.duelmasters.Services.BoosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/booster")
public class BoosterController {

    private final BoosterService boosterService;

    @Autowired
    public BoosterController(BoosterService boosterService) {

        this.boosterService = boosterService;
    }

    @PostMapping("/buy")
    public ResponseEntity<List<CardResponse>> buyBooster(
            @RequestBody @Valid BuyBoosterRequest request) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<CardResponse> response = boosterService.buyBooster(
                request, userId);

        return ResponseEntity.ok(response);
    }
}
