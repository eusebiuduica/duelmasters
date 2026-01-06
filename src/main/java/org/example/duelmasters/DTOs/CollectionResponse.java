package org.example.duelmasters.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionResponse {
    Integer id;
    String name;
    Integer quantity;
    Integer inPackage;
}
