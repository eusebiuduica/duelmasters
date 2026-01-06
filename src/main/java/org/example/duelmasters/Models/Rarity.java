package org.example.duelmasters.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "rarities")
@Getter
@Setter
public class Rarity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "sell_gold")
    private Integer sellGold;

    @OneToMany(mappedBy = "rarity")
    @JsonIgnore
    private Set<Card> cards;
}