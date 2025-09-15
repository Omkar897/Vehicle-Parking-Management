package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.model.User;
import com.example.repository.UserRepository;
import java.util.Optional;

@Component
public class AdminUserInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if admin user already exists
            Optional<User> existingAdmin = userRepository.findByUsername("admin");
            
            if (existingAdmin.isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");
                admin.setFullName("System Administrator");
                admin.setEmail("admin@parkeasy.com");
                admin.setPhoneNumber("+1-555-ADMIN");
                
                userRepository.save(admin);
                System.out.println("✅ Default admin user created successfully!");
                System.out.println("   Username: admin");
                System.out.println("   Password: admin");
                System.out.println("   Role: ADMIN");
            } else {
                System.out.println("ℹ️  Admin user already exists, skipping creation.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
