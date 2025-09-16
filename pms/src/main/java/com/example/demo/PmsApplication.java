package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling; // 🎯 ADD THIS
import org.springframework.transaction.annotation.EnableTransactionManagement; // 🎯 ADD THIS

import java.util.TimeZone; // 🎯 ADD THIS

@SpringBootApplication
@ComponentScan(basePackages = {"com.example"})
@EntityScan(basePackages = "com.example.model")
@EnableJpaRepositories(basePackages = "com.example.repository")
@EnableScheduling // 🎯 CRITICAL - Without this, your booking expiry scheduler won't work!
@EnableTransactionManagement // 🎯 CRITICAL - Without this, booking completion might not persist!
public class PmsApplication {

    public static void main(String[] args) {
        // 🎯 Force JVM timezone to IST
        System.setProperty("user.timezone", "Asia/Kolkata");
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        SpringApplication.run(PmsApplication.class, args);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
