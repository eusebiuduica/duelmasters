package org.example.duelmasters.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateEvent {
    private Integer id;
    private Integer quantity;
}
