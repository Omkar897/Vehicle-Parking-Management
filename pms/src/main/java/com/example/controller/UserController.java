package com.example.controller;
import com.example.model.User;
import com.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        if (userService.existsByUsername(user.getUsername())) {
            response.put("success", false);
            response.put("message", "Username exists");
            return ResponseEntity.ok(response);
        }
        if (userService.existsByEmail(user.getEmail())) {
            response.put("success", false);
            response.put("message", "Email exists");
            return ResponseEntity.ok(response);
        }

        User savedUser = userService.registerUser(user);
        if (savedUser != null) {
            response.put("success", true);
            response.put("message", "Registration successful");
        } else {
            response.put("success", false);
            response.put("message", "Registration failed");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Email does not exist. Please register.");
        }

        User user = userOpt.get();
        if (!userService.validatePassword(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Incorrect password.");
        }

        session.setAttribute("userId", user.getId());
        return ResponseEntity.ok("Login successful.");
    }



    // Add this method anywhere inside UserController class
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build(); // Unauthorized if no session
        }

        User user = userService.findById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
