package com.hometown.store.liquorstoreapp.controller;

import com.hometown.store.liquorstoreapp.dto.AuthRequest;
import com.hometown.store.liquorstoreapp.dto.AuthResponse;
import com.hometown.store.liquorstoreapp.dto.SignupRequest;
import com.hometown.store.liquorstoreapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation failed", result));
        }
        
        try {
            AuthResponse authResponse = authService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed. Please try again."));
        }
    }
    
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody AuthRequest authRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Invalid credentials", result));
        }
        
        try {
            AuthResponse authResponse = authService.signin(authRequest);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid username/email or password"));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed. Please try again."));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Refresh token is required"));
        }
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid or expired refresh token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Token refresh failed. Please login again."));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT setup, logout is handled on the client side
        // by removing the tokens. Optionally, you can implement token blacklisting
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication endpoint is working");
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message, BindingResult result) {
        Map<String, Object> response = createErrorResponse(message);
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        response.put("errors", errors);
        return response;
    }
}