package org.example.duelmasters.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "boosters")
@Getter
@Setter
public class Booster {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Integer price;

    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "booster")
    @JsonIgnore
    private Set<Card> cards;
}