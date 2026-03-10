package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.BuyOrderRequest;
import org.example.duelmasters.DTOs.MarketPlaceResponse;
import org.example.duelmasters.DTOs.MarketplaceOrderRequest;
import org.example.duelmasters.Models.MarketplaceOrder;
import org.example.duelmasters.Repositories.MarketplaceOrderRepository;
import org.example.duelmasters.Services.JwtService;
import org.example.duelmasters.Services.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping(path = "/marketplace")
public class MarketplaceOrderController {

    private final MarketplaceService marketplaceService;
    private final MarketplaceOrderRepository marketplaceOrderRepository;
    private final JwtService jwtService;

    @Autowired
    public MarketplaceOrderController
            (MarketplaceService marketplaceService,
             MarketplaceOrderRepository marketplaceOrderRepository,
             JwtService jwtService) {

        this.marketplaceService = marketplaceService;
        this.marketplaceOrderRepository = marketplaceOrderRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/add")
    public ResponseEntity<MarketPlaceResponse> addOrder(@RequestBody @Valid MarketplaceOrderRequest request) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(marketplaceService.addOrder(request, userId));
    }

    @PostMapping("/buy")
    public ResponseEntity<Integer> buyOrder(@RequestBody @Valid BuyOrderRequest request) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(marketplaceService.buyOrder(request, userId));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeOrder(@RequestBody Integer orderId) {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        marketplaceService.removeOrder(userId, orderId);
        return ResponseEntity.ok("Order remove successful!");
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<MarketPlaceResponse>> getAllOrders() {

        return ResponseEntity.ok(marketplaceService.getAll());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String token) {

        Integer userId = jwtService.extractUserId(token);
        return marketplaceService.addClient(userId);
    }
}
