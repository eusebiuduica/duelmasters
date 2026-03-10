package org.example.duelmasters.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegister {
    @NotBlank(
            message = "Username must be not empty!\r\n"
    )
    private String username;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must contain uppercase, lowercase, at least one number and at least 8 characters!\n"
    )
    private String password;


    @NotNull(message = "Civilization is required!\n")
    @Min(value = 1, message = "Invalid civilization!\n")
    @Max(value = 5, message = "Invalid civilization!\n")
    private Integer civilization;

}
