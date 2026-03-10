package org.example.duelmasters.Controllers;

import org.example.duelmasters.DTOs.UserDetailsResponse;
import org.example.duelmasters.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponse> me() {

        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }
}
