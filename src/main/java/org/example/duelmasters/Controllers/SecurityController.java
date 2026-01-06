package org.example.duelmasters.Controllers;

import jakarta.validation.Valid;
import org.example.duelmasters.DTOs.UserLogin;
import org.example.duelmasters.DTOs.UserRegister;
import org.example.duelmasters.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
public class SecurityController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody @Valid UserLogin dto) {
        return userService.authenticate(dto.getUsername(), dto.getPassword());
    }

    @GetMapping("/whoami")
    public String protectedWhoami() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return "You are: " + authentication.getName() + " with authorities " + authentication.getAuthorities();
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody @Valid UserRegister dto) {
        userService.addUser(dto.getUsername(), dto.getPassword(), dto.getCivilization());
        return ResponseEntity.status(HttpStatus.CREATED).body(true);
    }

    @DeleteMapping("delete")
    public ResponseEntity<Boolean> delete() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(true);
    }
}
