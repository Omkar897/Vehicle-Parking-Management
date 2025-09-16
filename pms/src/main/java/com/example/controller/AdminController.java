package com.example.controller;

import com.example.dto.BookingResponseDTO;
import com.example.dto.ParkingStatsDTO;
import com.example.model.*;
import com.example.repository.BookingRepository;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin")
public class AdminController {
 
 @Autowired
 private UserService userService;
 
 @Autowired
 private ParkingSlotService parkingSlotService;
 
 @Autowired
 private BookingService bookingService;
 
 @Autowired
 private PaymentService paymentService;
 
 @Autowired
 private PasswordEncoder passwordEncoder;

 @Autowired
 private BookingRepository bookingRepository;
 
 // Admin login
 @PostMapping("/login")
 public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> loginData) {
     Map<String, Object> response = new HashMap<>();
     String username = loginData.get("username");
     String password = loginData.get("password");

     Optional<User> userOpt = userService.findByUsername(username);

     if (userOpt.isPresent()) {
         User user = userOpt.get();
         if ("ADMIN".equals(user.getRole()) && passwordEncoder.matches(password, user.getPassword())) {
             response.put("success", true);
             response.put("message", "Admin login successful!");
             response.put("user", user);
             return ResponseEntity.ok(response);
         } else if (!"ADMIN".equals(user.getRole())) {
             response.put("success", false);
             response.put("message", "Access denied! You are not an admin.");
             return ResponseEntity.ok(response);
         } else {
             response.put("success", false);
             response.put("message", "Invalid password!");
             return ResponseEntity.ok(response);
         }
     } else {
         response.put("success", false);
         response.put("message", "Admin not found!");
         return ResponseEntity.ok(response);
     }
 }

 // Get dashboard statistics
 @GetMapping("/dashboard/stats")
 public ResponseEntity<Map<String, Object>> getDashboardStats() {
     Map<String, Object> stats = new HashMap<>();
     
     stats.put("totalSlots", parkingSlotService.getTotalSlots());
     stats.put("availableSlots", parkingSlotService.getAvailableSlotsCount());
     stats.put("occupiedSlots", parkingSlotService.getOccupiedSlotsCount());
     
     stats.put("twoWheelerSlots", parkingSlotService.getTwoWheelerSlotsCount());
     stats.put("threeWheelerSlots", parkingSlotService.getThreeWheelerSlotsCount());
     stats.put("fourWheelerSlots", parkingSlotService.getFourWheelerSlotsCount());

     stats.put("evSlots", parkingSlotService.getEVSlotsCount());
     stats.put("availableEvSlots", parkingSlotService.getAvailableEVSlotsCount());

     stats.put("availableTwoWheelerSlots", parkingSlotService.getAvailableTwoWheelerSlotsCount());
     stats.put("availableThreeWheelerSlots", parkingSlotService.getAvailableThreeWheelerSlotsCount());
     stats.put("availableFourWheelerSlots", parkingSlotService.getAvailableFourWheelerSlotsCount());

     stats.put("totalBookings", bookingService.getTotalBookingsCount());
     stats.put("todayBookings", bookingService.getTodayBookingsCount());

     // Calculate total revenue
     double totalRevenue = paymentService.getAllPayments().stream()
         .filter(payment -> "SUCCESS".equals(payment.getStatus()))
         .mapToDouble(Payment::getAmount)
         .sum();
     stats.put("totalRevenue", totalRevenue);
     
     return ResponseEntity.ok(stats);
 }

 @GetMapping("/bookings")
 public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
     List<Booking> bookings = bookingService.getAllBookings();
     List<BookingResponseDTO> dtos = bookings.stream()
             .map(bookingService::mapToDTO)
             .collect(Collectors.toList());
     return ResponseEntity.ok(dtos);
 }

    @PostMapping("/bookings/status")
    public ResponseEntity<?> updateBookingStatus(@RequestBody Map<String, Object> request) {
        try {
            Long bookingId = Long.valueOf(request.get("bookingId").toString());
            String status = request.get("status").toString();

            // Update booking status in database
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                booking.setStatus(status);
                bookingRepository.save(booking);

                // Update slot availability based on status
                Long slotId = booking.getParkingSlot().getId();
                if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                    parkingSlotService.updateSlotAvailability(slotId, true);
                } else if ("BOOKED".equals(status)) {
                    parkingSlotService.updateSlotAvailability(slotId, false);
                }

                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get all users
 @GetMapping("/users")
 public ResponseEntity<List<User>> getAllUsers() {
     List<User> users = userService.getAllUsers();
     // Remove password field from response
     users.forEach(user -> user.setPassword(null));
     return ResponseEntity.ok(users);
 }
 
 // Create parking slot
 @PostMapping("/slots")
 public ResponseEntity<Map<String, Object>> createParkingSlot(@RequestBody ParkingSlot slot) {
     Map<String, Object> response = new HashMap<>();
     
     try {
         ParkingSlot existingSlot = parkingSlotService.getSlotByNumber(slot.getSlotNumber());
         if (existingSlot != null) {
             response.put("success", false);
             response.put("message", "Slot number already exists!");
             return ResponseEntity.ok(response);
         }
         
         ParkingSlot newSlot = parkingSlotService.saveSlot(slot);
         response.put("success", true);
         response.put("message", "Parking slot created successfully!");
         response.put("slot", newSlot);
         return ResponseEntity.ok(response);
     } catch (Exception e) {
         response.put("success", false);
         response.put("message", "Failed to create parking slot: " + e.getMessage());
         return ResponseEntity.ok(response);
     }
 }
 
 // Update parking slot
 @PutMapping("/slots/{id}")
 public ResponseEntity<Map<String, Object>> updateParkingSlot(@PathVariable Long id, @RequestBody ParkingSlot slot) {
     Map<String, Object> response = new HashMap<>();
     
     try {
         ParkingSlot existingSlot = parkingSlotService.getSlotById(id);
         if (existingSlot == null) {
             response.put("success", false);
             response.put("message", "Parking slot not found!");
             return ResponseEntity.ok(response);
         }
         
         // Check if slot number is being changed to an existing one
         if (!existingSlot.getSlotNumber().equals(slot.getSlotNumber())) {
             ParkingSlot slotWithNumber = parkingSlotService.getSlotByNumber(slot.getSlotNumber());
             if (slotWithNumber != null) {
                 response.put("success", false);
                 response.put("message", "Slot number already exists!");
                 return ResponseEntity.ok(response);
             }
         }
         
         slot.setId(id);
         ParkingSlot updatedSlot = parkingSlotService.saveSlot(slot);
         response.put("success", true);
         response.put("message", "Parking slot updated successfully!");
         response.put("slot", updatedSlot);
         return ResponseEntity.ok(response);
     } catch (Exception e) {
         response.put("success", false);
         response.put("message", "Failed to update parking slot: " + e.getMessage());
         return ResponseEntity.ok(response);
     }
 }
 
 // Delete parking slot
 @DeleteMapping("/slots/{id}")
 public ResponseEntity<Map<String, Object>> deleteParkingSlot(@PathVariable Long id) {
     Map<String, Object> response = new HashMap<>();
     
     try {
         ParkingSlot slot = parkingSlotService.getSlotById(id);

         if (slot == null) {
             response.put("success", false);
             response.put("message", "Parking slot not found!");
             return ResponseEntity.ok(response);
         }
         
         // Check if slot has active bookings
         List<Booking> activeBookings = parkingSlotService.getActiveBookingsForSlot(id);
         if (!activeBookings.isEmpty()) {
             response.put("success", false);
             response.put("message", "Cannot delete slot with active bookings!");
             return ResponseEntity.ok(response);
         }
         
         parkingSlotService.deleteSlot(id);
         response.put("success", true);
         response.put("message", "Parking slot deleted successfully!");
         return ResponseEntity.ok(response);
     } catch (Exception e) {
         response.put("success", false);
         response.put("message", "Failed to delete parking slot: " + e.getMessage());
         return ResponseEntity.ok(response);
     }
 }

    // Get all parking slots
    @GetMapping("/slots")
    public ResponseEntity<List<ParkingSlot>> getAllParkingSlots() {
        return ResponseEntity.ok(parkingSlotService.getAllSlots());
    }

    // Get all payments
 @GetMapping("/payments")
 public ResponseEntity<List<Payment>> getAllPayments() {
     return ResponseEntity.ok(paymentService.getAllPayments());
 }

    @GetMapping("/dashboard/totalRevenue")
    public ResponseEntity<Double> getTotalRevenue() {
        double totalRevenue = paymentService.getAllPayments().stream()
                .filter(payment -> "SUCCESS".equals(payment.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();

        return ResponseEntity.ok(totalRevenue);
    }
}
