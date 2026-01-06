package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.BuyOrderRequest;
import org.example.duelmasters.DTOs.MarketplaceOrderRequest;
import org.example.duelmasters.Models.MarketplaceOrder;
import org.example.duelmasters.Repositories.MarketplaceOrderRepository;
import org.example.duelmasters.Services.DeckService;
import org.example.duelmasters.Services.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/marketplace")
public class MarketplaceOrderController {

    private final MarketplaceService marketplaceService;
    private final MarketplaceOrderRepository marketplaceOrderRepository;

    @Autowired
    public MarketplaceOrderController(MarketplaceService marketplaceService,  MarketplaceOrderRepository marketplaceOrderRepository) {
        this.marketplaceService = marketplaceService;
        this.marketplaceOrderRepository = marketplaceOrderRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addOrder(
            @RequestBody @Valid MarketplaceOrderRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        marketplaceService.addOrder(request, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyOrder(@RequestBody @Valid BuyOrderRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        marketplaceService.buyOrder(request, userId);
        return ResponseEntity.ok("Purchase successful!");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeOrder(@RequestBody Integer orderId) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        marketplaceService.removeOrder(userId, orderId);
        return ResponseEntity.ok("Order remove successful!");
    }

    @GetMapping("/html")
    public String getMarketplaceHtml() {
        // aici multe filtre
        List<MarketplaceOrder> orders = marketplaceOrderRepository.findAll();

        String baseUrl = "http://localhost:8080";

        StringBuilder sb = new StringBuilder(
                "<html><body>" +
                        "<h2>Marketplace</h2>" +
                        "<div style='display:grid;grid-template-columns:repeat(5,1fr);gap:10px;'>"
        );

        for (MarketplaceOrder o : orders) {

            if (o.getQuantity() <= 0)
                continue;

            sb.append("<div style='text-align:center;border:1px solid #ccc;padding:8px;'>")
                    .append("<img src='").append(baseUrl)
                    .append("/").append(o.getCard().getImage())
                    .append("' style='width:100px;height:150px;'/><br/>")

                    .append("<b>").append(o.getCard().getName()).append("</b><br/>")
                    .append("Seller: ").append(o.getUser().getUsername()).append("<br/>")
                    .append("Qty: ").append(o.getQuantity()).append("<br/>")
                    .append("Price: ").append(o.getPrice()).append(" gold")
                    .append("</div>");
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }
}
