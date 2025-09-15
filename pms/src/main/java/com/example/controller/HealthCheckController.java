package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;

@RestController
public class HealthCheckController {
    
    @GetMapping("/")
    public Map<String, String> healthCheck() {
        return Collections.singletonMap("status", "UP");
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Collections.singletonMap("status", "UP");
    }
}
