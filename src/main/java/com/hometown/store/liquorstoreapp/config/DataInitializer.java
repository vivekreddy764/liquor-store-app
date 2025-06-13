package com.hometown.store.liquorstoreapp.config;

import com.hometown.store.liquorstoreapp.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final AuthService authService;
    
    public DataInitializer(AuthService authService) {
        this.authService = authService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createAdminUser("admin", "admin@liquorstore.com", "Admin@123456");
        System.out.println("Default admin user created: admin / Admin@123456");
    }
}