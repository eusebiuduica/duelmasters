package org.example.duelmasters.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardSellResponse {
    private Integer id;
    private String image;
    private int quantity;
    private int sellGold;
}

