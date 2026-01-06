package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegister {
    @NotBlank(
            message = "Username must be not empty!"
    )
    private String username;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must contain uppercase, lowercase, at least one number and at least 8 characters!"
    )
    private String password;


    @NotNull(message = "Civilization is required!")
    @Min(value = 1, message = "Invalid civilization!")
    @Max(value = 5, message = "Invalid civilization!")
    private Integer civilization;

}
