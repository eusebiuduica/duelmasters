package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Colection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "in_package")
    private Integer inPackage;
}
