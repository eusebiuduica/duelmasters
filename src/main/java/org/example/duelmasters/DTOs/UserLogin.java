package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLogin {
    @NotBlank(
            message = "Field must be not empty!"
    )
    private String username;

    @NotBlank(
            message = "Field must be not empty!"
    )
    private String password;
}
