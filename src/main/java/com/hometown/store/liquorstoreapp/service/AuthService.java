package com.hometown.store.liquorstoreapp.service;

import com.hometown.store.liquorstoreapp.dto.AuthRequest;
import com.hometown.store.liquorstoreapp.dto.AuthResponse;
import com.hometown.store.liquorstoreapp.dto.SignupRequest;
import com.hometown.store.liquorstoreapp.entity.User;
import com.hometown.store.liquorstoreapp.repository.UserRepository;
import com.hometown.store.liquorstoreapp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    public AuthResponse signup(SignupRequest request) {
        // Validate password confirmation
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setFailedLoginAttempts(0);
        
        user = userRepository.save(user);
        
        // Generate tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        return new AuthResponse(accessToken, refreshToken, user);
    }
    
    public AuthResponse signin(AuthRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account is locked due to too many failed login attempts");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
            
            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                userRepository.updateFailedLoginAttempts(user.getUsername(), 0);
            }
            
            // Update last login time
            userRepository.updateLastLogin(user.getUsername(), LocalDateTime.now());
            
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            
            return new AuthResponse(accessToken, refreshToken, user);
            
        } catch (AuthenticationException e) {
            // Increment failed login attempts
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            userRepository.updateFailedLoginAttempts(user.getUsername(), failedAttempts);
            
            // Lock account if max attempts reached
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                userRepository.updateAccountLocked(user.getUsername(), false);
                throw new LockedException("Account locked due to too many failed login attempts");
            }
            
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        
        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
        
        return new AuthResponse(newAccessToken, newRefreshToken, user);
    }
    
    public void createAdminUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            return; // Admin already exists
        }
        
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setAccountNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setCredentialsNonExpired(true);
        admin.setFailedLoginAttempts(0);
        
        userRepository.save(admin);
    }
}