package com.example.service;
import com.example.service.ParkingSlotService;
import com.example.model.Booking;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;


    @Autowired
    private ParkingSlotService parkingService;


    @Autowired
    public PasswordEncoder passwordEncoder;

    // Register a new user
    @Transactional
    public User registerUser(User user) {
        System.out.println("Registering user: " + user.getUsername() + ", email: " + user.getEmail());

        if (userRepository.existsByUsername(user.getUsername()) ||
                userRepository.existsByEmail(user.getEmail())) {
            System.out.println("User already exists!");
            return null;
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        System.out.println("User saved with ID: " + savedUser.getId());
        return savedUser;
    }

    // Validate raw password against encoded password

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }


    // Find user by username
    public Optional<User> findByUsername(String username) {

        return userRepository.findByUsername(username);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    // Check if username exists
    public boolean existsByUsername(String username) {

        return userRepository.existsByUsername(username);
    }

    // Check if email exists
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }
    public User findById(Long id) {

        return userRepository.findById(id).orElse(null);
    }



    // Get all users (without passwords)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // remove passwords
        return users;
    }


    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public void deleteUserAndCleanup(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Booking> bookings = bookingRepository.findByUser(user);
            for (Booking booking : bookings) {
                parkingService.updateSlotAvailability(booking.getParkingSlot().getId(), true);
                bookingRepository.delete(booking);
            }
            userRepository.delete(user);
        }
    }

}

