package org.example.duelmasters.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MarketPlaceResponse {

    Integer id;
    Integer cardId;
    String seller;
    Integer quantity;
    Integer price;
    String cardImage;
}
