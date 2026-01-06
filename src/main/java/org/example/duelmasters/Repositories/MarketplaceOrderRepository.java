package org.example.duelmasters.Repositories;

import org.example.duelmasters.Models.Card;
import org.example.duelmasters.Models.MarketplaceOrder;
import org.example.duelmasters.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, Integer> {

    List<MarketplaceOrder> findByCard(Card card);

    List<MarketplaceOrder> findByUser(User user);
}
