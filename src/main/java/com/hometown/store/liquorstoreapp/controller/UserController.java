package com.hometown.store.liquorstoreapp.controller;

import com.hometown.store.liquorstoreapp.dto.AuthResponse;
import com.hometown.store.liquorstoreapp.entity.User;
import com.hometown.store.liquorstoreapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal User user) {
        try {
            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Unable to fetch user profile");
        }
    }
}