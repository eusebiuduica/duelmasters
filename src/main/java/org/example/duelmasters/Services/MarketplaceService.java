package org.example.duelmasters.Services;

import lombok.RequiredArgsConstructor;
import org.example.duelmasters.DTOs.BuyOrderRequest;
import org.example.duelmasters.DTOs.MarketPlaceResponse;
import org.example.duelmasters.DTOs.MarketplaceOrderRequest;
import org.example.duelmasters.DTOs.OrderUpdateEvent;
import org.example.duelmasters.Infrastructure.AllSseManager;
import org.example.duelmasters.Models.*;
import org.example.duelmasters.Repositories.*;
import org.example.duelmasters.Utils.AuditAction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CollectionRepository collectionRepository;
    private final MarketplaceOrderRepository marketplaceOrderRepository;
    private final AuditLogRepository auditLogRepository;
    private final AllSseManager sseManager;

    @Transactional
    public MarketPlaceResponse addOrder(MarketplaceOrderRequest request, Integer userId) {

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Price must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found"));

        Colection collection = collectionRepository
                .findByUserAndCard(user, card)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "User does not own this card"));

        if (collection.getQuantity() < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough available copies to sell");
        }

        // Lock cards for marketplace
        collection.setQuantity(
                collection.getQuantity() - request.getQuantity()
        );

        MarketplaceOrder order = MarketplaceOrder.builder()
                .user(user)
                .card(card)
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();

        marketplaceOrderRepository.save(order);

        MarketPlaceResponse response = new MarketPlaceResponse(order.getId(), card.getId(), user.getUsername(), order.getQuantity(), order.getPrice(), card.getImage());
        sseManager.broadcast(response, "PRODUCT_ADDED");
        return response;
    }

    @Transactional
    public Integer buyOrder(BuyOrderRequest request, Integer userId) {

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!"));

        MarketplaceOrder order = marketplaceOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found!"));

        if (order.getUser().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot buy your own order!");
        }

        if (request.getQuantity() <= 0 || request.getQuantity() > order.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid quantity requested!");
        }

        int totalPrice = request.getQuantity() * order.getPrice();

        if (buyer.getGold() < totalPrice) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not enough gold!");
        }

        // Deduct gold from buyer
        buyer.setGold(buyer.getGold() - totalPrice);

        // Add gold to seller
        User seller = order.getUser();
        seller.setGold(seller.getGold() + totalPrice);

        // Add card to buyer's collection
        Colection buyerCollection = collectionRepository
                .findByUserAndCard(buyer, order.getCard())
                .orElseGet(() -> {
                    Colection newCol = new Colection();
                    newCol.setUser(buyer);
                    newCol.setCard(order.getCard());
                    newCol.setQuantity(0);
                    newCol.setInPackage(0);
                    return newCol;
                });

        buyerCollection.setQuantity(buyerCollection.getQuantity() + request.getQuantity());
        collectionRepository.save(buyerCollection);

        // Reduce quantity in marketplace
        order.setQuantity(order.getQuantity() - request.getQuantity());

        StringBuilder sbInfo = new StringBuilder("Sold - [");
        sbInfo.append(order.getCard().getName())
                .append("] x").append(request.getQuantity()).append(" copies for ")
                .append(totalPrice).append(" gold");

        if (order.getQuantity() == 0) {
            marketplaceOrderRepository.delete(order);
            sseManager.broadcast(order.getId(), "ORDER_DELETED");
        } else {
            OrderUpdateEvent orderUpdateEvent = new OrderUpdateEvent();
            orderUpdateEvent.setId(order.getId());
            orderUpdateEvent.setQuantity(order.getQuantity());

            marketplaceOrderRepository.save(order);
            sseManager.broadcast(orderUpdateEvent, "ORDER_UPDATED");
        }

        sseManager.notifySale(seller.getId(), sbInfo.toString(),seller.getGold());

        StringBuilder sbAuditDetails = new StringBuilder("Bought card from marketplace: ");
        sbAuditDetails.append(order.getCard().getName())
                .append(" (ID=").append(order.getCard().getId()).append(")")
                .append(" x").append(request.getQuantity())
                .append(" for price: ").append(totalPrice);

        AuditLog auditLog = new AuditLog();
        auditLog.setActorUser(buyer);
        auditLog.setActorUsername(buyer.getUsername());
        auditLog.setTargetUser(seller);
        auditLog.setTargetUsername(seller.getUsername());
        auditLog.setAction(AuditAction.CARD_BUY);
        auditLog.setDetails(sbAuditDetails.toString());
        auditLogRepository.save(auditLog);

        return buyer.getGold();
    }

    @Transactional
    public void removeOrder(Integer userId, Integer orderId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        MarketplaceOrder order = marketplaceOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found");
        }

        // Return cards to collection
        Optional<Colection> collection = collectionRepository
                .findByUserAndCard(user, order.getCard());

        // user has this card registered in collection, otherwise they couldn't place it in marketplace

        int nbCards = collection.get().getQuantity();
        collection.get().setQuantity(nbCards + order.getQuantity());

        marketplaceOrderRepository.delete(order);
        sseManager.broadcast(order.getId(), "ORDER_DELETED");
    }

    public List<MarketPlaceResponse> getAll()
    {
        List<MarketPlaceResponse> marketplaceResponseList = new ArrayList<>();

        List<MarketplaceOrder> orders = marketplaceOrderRepository.findAll();

        for (MarketplaceOrder order : orders) {

            Card card = order.getCard();

            MarketPlaceResponse response = new MarketPlaceResponse(order.getId(), card.getId(), order.getUser().getUsername(), order.getQuantity(), order.getPrice(), card.getImage());

            marketplaceResponseList.add(response);
        }

        return marketplaceResponseList;
    }

    public SseEmitter addClient(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return sseManager.addClient(userId);
    }
}
