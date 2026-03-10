package org.example.duelmasters.DTOs.Booster;

import lombok.Getter;
import lombok.Setter;
import org.example.duelmasters.DTOs.CardResponse;

import java.util.List;

@Getter
@Setter
public class BuyBoosterResponse {

    private List<CardResponse> cards;
    private int goldLeft;
}
