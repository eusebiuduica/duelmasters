package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "civilization")
    private Civilization civilization;

    @ManyToOne
    @JoinColumn(name = "type")
    private Type type;

    @Column
    private Integer cost;

    @Column
    private Integer power;

    @ManyToOne
    @JoinColumn(name = "rarity")
    private Rarity rarity;

    @ManyToOne
    @JoinColumn(name = "booster")
    private Booster booster;

    @Column(length = 20)
    private String race;

    @Column(columnDefinition = "TEXT")
    private String effect;

    @Column(columnDefinition = "TEXT")
    private String image;

    @OneToMany(mappedBy = "card")
    private List<Colection> colections;

    @OneToMany(mappedBy = "card")
    private Set<DeckCard> decks;
}
