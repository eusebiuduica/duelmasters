package org.example.duelmasters.Services;

import org.example.duelmasters.DTOs.UserDetailsResponse;
import org.example.duelmasters.DTOs.UserLoginResponse;
import org.example.duelmasters.Infrastructure.MarketplaceSseManager;
import org.example.duelmasters.Models.*;
import org.example.duelmasters.Repositories.CardRepository;
import org.example.duelmasters.Repositories.CivilizationRepository;
import org.example.duelmasters.Repositories.CollectionRepository;
import org.example.duelmasters.Repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CivilizationRepository civilizationRepository;
    private final CardRepository cardRepository;
    private final CollectionRepository collectionRepository;
    private final MarketplaceSseManager marketplaceSseManager;

    @Transactional
    public void addUser(String username, String password, Integer civilizationId) {
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already taken!"
            );
        }

        Civilization civ = civilizationRepository.findById(civilizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Civilization not found!"
                ));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        user.setCivilization(civ);
        user.setLastLogin(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        // Light main    - Water second
        // Water main    - Darkness second
        // Darkness main - Fire second
        // Fire main     - Nature second
        // Nature main   - Light second
        // main civilization 3 from each common, 2 from each uncommon, 1 from each rare
        // second civilization 2 from each common, 1 from each uncommon

        int secondCivilizationId = civilizationId + 1;
        if (secondCivilizationId == 6)
            secondCivilizationId = 1;

        List<Card> commonsMain = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityIdAndCivilizationId(1, 1, civilizationId));
        for (Card card : commonsMain) {

            Colection c = Colection.builder()
                    .user(user)
                    .card(card)
                    .quantity(3)
                    .inPackage(0)
                    .build();
            collectionRepository.save(c);
        }

        List<Card> uncommonsMain = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityIdAndCivilizationId(1, 2, civilizationId));
        for (Card card : uncommonsMain) {

            Colection c = Colection.builder()
                    .user(user)
                    .card(card)
                    .quantity(2)
                    .inPackage(0)
                    .build();
            collectionRepository.save(c);
        }

        List<Card> raresMain = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityIdAndCivilizationId(1, 3, civilizationId));
        for (Card card : raresMain) {

            Colection c = Colection.builder()
                    .user(user)
                    .card(card)
                    .quantity(1)
                    .inPackage(0)
                    .build();
            collectionRepository.save(c);
        }

        List<Card> commonsSecond = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityIdAndCivilizationId(1, 1, secondCivilizationId));
        for (Card card : commonsSecond) {

            Colection c = Colection.builder()
                    .user(user)
                    .card(card)
                    .quantity(2)
                    .inPackage(0)
                    .build();
            collectionRepository.save(c);
        }

        List<Card> uncommonsSecond = new ArrayList<>(cardRepository.findAllByBoosterIdAndRarityIdAndCivilizationId(1, 2, secondCivilizationId));
        for (Card card : uncommonsSecond) {

            Colection c = Colection.builder()
                    .user(user)
                    .card(card)
                    .quantity(1)
                    .inPackage(0)
                    .build();
            collectionRepository.save(c);
        }
    }

    @Transactional
    public UserLoginResponse authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials!");
        }

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate todayUtc = nowUtc.toLocalDate();
            UserLoginResponse response = new UserLoginResponse();
            response.setDailyReward(false);
            response.setGoldReceived(0);
            if (user.getLastLogin() == null || !user.getLastLogin().toLocalDate().equals(todayUtc)) {
                // here login bonus and increment login days - once per day
                user.setGold(user.getGold() + 10);
                user.setLoginDays(user.getLoginDays() + 1);
                response.setDailyReward(true);
                response.setGoldReceived(10);
            }
        response.setTotalGold(user.getGold());
        user.setLastLogin(nowUtc);
        userRepository.save(user);
        response.setToken(jwtService.createToken(username, user.getId()));
        return response;
    }

    public User validateUser(String token) {
        if (!jwtService.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!");
        }

        Integer userId = jwtService.extractUserId(token);

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!"));
    }


    @Transactional
    public void deleteUser(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            for (MarketplaceOrder marketplaceOrder : user.get().getMarketplaceOrders()) {
                marketplaceSseManager.broadcast(marketplaceOrder.getId(), "ORDER_DELETED");
            }
            userRepository.delete(user.get());
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!");
        }
    }

    public UserDetailsResponse getUserDetails(int userId)
    {
        UserDetailsResponse response = new UserDetailsResponse();
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            response.setGold(user.get().getGold());
            response.setUsername(user.get().getUsername());
            return response;
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
    }
}
