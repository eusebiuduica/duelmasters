package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLogin {
    @NotBlank(
            message = "Username must not be empty!"
    )
    private String username;

    @NotBlank(
            message = "Password must not be empty!"
    )
    private String password;
}
