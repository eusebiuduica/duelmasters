package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionFilter {

    @Min(value = 1, message = "Invalid civilization!")
    @Max(value = 5, message = "Invalid civilization!")
    private Integer civilization;   // optional

    @Min(value = 1, message = "Invalid rarity!")
    @Max(value = 5, message = "Invalid rarity!")
    private Integer rarity;         // optional

    @Min(value = 1, message = "Invalid type!")
    @Max(value = 3, message = "Invalid type!")
    private Integer type;           // optional

    @Min(value = 1, message = "Mana min must be at least 1!")
    private Integer manaMin;        // optional

    @Min(value = 0, message = "Mana max must be at least 1!")
    private Integer manaMax;        // optional

    @Min(value = 0, message = "Power min must positive!")
    private Integer powerMin;       // optional

    @Min(value = 0, message = "Power max must positive!")
    private Integer powerMax;       // optional

    private String sortBy;          // mana / power / id
    private String sortDir;         // asc / desc
}

