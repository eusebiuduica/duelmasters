package org.example.duelmasters.DTOs.Booster;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoosterResponse {

    private int id;
    private String name;
    private int price;
    private String image;
    private Integer quantity;
}
