package com.hometown.store.liquorstoreapp.repository;

import com.hometown.store.liquorstoreapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByEnabledTrue();
    
    List<User> findByEnabledFalse();
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    List<User> findUsersWithFailedAttempts(@Param("attempts") Integer attempts);
    
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.username = :username")
    void updateFailedLoginAttempts(@Param("username") String username, @Param("attempts") Integer attempts);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.username = :username")
    void updateLastLogin(@Param("username") String username, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = :locked WHERE u.username = :username")
    void updateAccountLocked(@Param("username") String username, @Param("locked") Boolean locked);
    
    long countByRole(User.Role role);
    
    long countByEnabled(Boolean enabled);
}