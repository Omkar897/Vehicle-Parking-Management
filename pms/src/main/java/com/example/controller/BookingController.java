package com.example.controller;

import com.example.dto.BookingRequestDTO;
import com.example.dto.BookingResponseDTO;
import com.example.dto.FrontendBookingDTO;
import com.example.model.Booking;
import com.example.model.ParkingSlot;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.service.BookingService;
import com.example.service.ParkingSlotService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO bookingRequest, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");
        }
        User user = userService.findById(userId);
        ParkingSlot slot = parkingSlotService.getSlotByNumber(bookingRequest.getSlotNumber());
        if (user == null || slot == null) {
            return ResponseEntity.badRequest().body("Invalid user or parking slot");
        }
        if (!slot.isAvailable()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Slot is not available");
        }
        // Define active booking statuses
        List<String> activeStatuses = List.of("BOOKED", "PAID");
        // Check for overlapping active bookings in the requested time range
        List<Booking> conflictingBookings = bookingRepository.findConflictingActiveBookings(
                slot.getId(), bookingRequest.getEntryTime(), bookingRequest.getExitTime(), activeStatuses);
        if (!conflictingBookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Slot already booked for the selected time");
        }
        // Create a new booking
        Booking booking = new Booking(user, slot, bookingRequest.getVehicleType(), bookingRequest.getEntryTime(), bookingRequest.getExitTime());
        // Mark slot as occupied
        parkingSlotService.updateSlotAvailability(slot.getId(), false);
        // Save booking
        bookingRepository.save(booking);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<?> getUserBookings(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");

        User user = userService.findById(userId);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        List<Booking> bookings = bookingService.getUserBookings(user);
        List<BookingResponseDTO> dtos = bookings.stream()
                .map(bookingService::mapToDTO)
                .peek(dto -> System.out.println("Mapped booking user name: " + (dto.getUser() != null ? dto.getUser().getUsername() : "null user")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");
        }
        Booking booking = bookingService.getBookingById(id);
        if (booking == null || !booking.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled");
    }
    
    @GetMapping("/frontend")
    public ResponseEntity<?> getUserBookingsForFrontend(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        List<Booking> bookings = bookingService.getUserBookings(user);

        List<FrontendBookingDTO> frontendBookings = bookings.stream().map(booking -> {
            FrontendBookingDTO dto = new FrontendBookingDTO();
            dto.setId(booking.getId());
            String slotNumber = booking.getParkingSlot() != null ? booking.getParkingSlot().getSlotNumber() : "Unknown";
            dto.setSlot(slotNumber);
            dto.setVehicleType(booking.getVehicleType() != null && !booking.getVehicleType().isEmpty() ? booking.getVehicleType() : "FOUR_WHEELER");
            dto.setEntryTime(booking.getEntryTime().toString());  // format as ISO string or use formatter
            dto.setExitTime(booking.getExitTime().toString());
            dto.setTotalAmount(booking.getTotalAmount());
            dto.setStatus(booking.getStatus());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(frontendBookings);
    }
}
