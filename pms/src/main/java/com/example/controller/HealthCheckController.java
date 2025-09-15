package com.example.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // Added base path to avoid conflicts
public class HealthCheckController implements HealthIndicator {
    
    @Value("${spring.application.name:parking-management}")
    private String appName;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "UP");
        details.put("service", appName);
        details.put("port", serverPort);
        details.put("timestamp", System.currentTimeMillis());
        
        return Health.up()
                .withDetails(details)
                .build();
    }
    
    // Removed the conflicting @GetMapping("/") 
    @GetMapping("/status") // This becomes /api/status
    public Map<String, String> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", appName);
        response.put("message", "Vehicle Parking Management System is running");
        return response;
    }
    
    @GetMapping("/health") // This becomes /api/health
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", appName);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
