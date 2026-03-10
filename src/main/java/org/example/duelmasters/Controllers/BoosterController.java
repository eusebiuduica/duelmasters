package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.Booster.BoosterResponse;
import org.example.duelmasters.DTOs.Booster.BuyBoosterRequest;
import org.example.duelmasters.DTOs.Booster.BuyBoosterResponse;
import org.example.duelmasters.Services.BoosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<BuyBoosterResponse> buyBooster(@RequestBody @Valid BuyBoosterRequest request) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BuyBoosterResponse response = boosterService.buyBooster(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BoosterResponse>> allBoosters() {

        return ResponseEntity.ok(boosterService.getAllBoosters());
    }
}
