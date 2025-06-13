package com.hometown.store.liquorstoreapp.service;

import com.hometown.store.liquorstoreapp.dto.UserUpdateRequest;
import com.hometown.store.liquorstoreapp.entity.User;
import com.hometown.store.liquorstoreapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getActiveUsers() {
        return userRepository.findByEnabledTrue();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getInactiveUsers() {
        return userRepository.findByEnabledFalse();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
    
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public User updateUser(String username, UserUpdateRequest request) {
        User user = getUserByUsername(username);
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }
            user.setEmail(request.getEmail());
        }
        
        return userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRole(Long userId, User.Role role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public User enableUser(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public User disableUser(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(false);
        return userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user");
            }
        }
        userRepository.delete(user);
    }
    
    @PreAuthorize("#username == authentication.name")
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getInactiveUsersSince(LocalDateTime date) {
        return userRepository.findInactiveUsers(date);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public long getUserCount() {
        return userRepository.count();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public long getActiveUserCount() {
        return userRepository.countByEnabled(true);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public long getAdminCount() {
        return userRepository.countByRole(User.Role.ADMIN);
    }
}