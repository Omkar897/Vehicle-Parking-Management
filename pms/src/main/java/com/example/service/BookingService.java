package com.example.service;
import com.example.dto.*;
import com.example.model.Booking;
import com.example.model.ParkingSlot;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.ParkingSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.TimeZone;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {
 
 @Autowired
 private BookingRepository bookingRepository;
 
 @Autowired
 private ParkingSlotService parkingSlotService;

public List<Booking> getAllBookings() {
    updateExpiredBookingsAndFreeSlots();  // update statuses before fetching
    return bookingRepository.findAllWithUserAndSlot();
}

    public List<Booking> getUserBookings(User user) {
        updateExpiredBookingsAndFreeSlots();
        return bookingRepository.findByUser(user);
    }
 
 public Booking getBookingById(Long id) {
     Optional<Booking> booking = bookingRepository.findById(id);
     return booking.orElse(null);
 }
 
 public Booking createBooking(User user, ParkingSlot parkingSlot,String vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
     // Check if the slot is available
     if (!parkingSlot.isAvailable()) {
         return null;
     }
     
     // Check for overlapping bookings - get all bookings for this slot except cancelled ones
     List<Booking> existingBookings = bookingRepository.findByParkingSlotIdAndStatusNot(
         parkingSlot.getId(), "CANCELLED");
     
     // Filter out the bookings that overlap with the new booking
     for (Booking existingBooking : existingBookings) {
         if (isOverlapping(existingBooking.getEntryTime(), existingBooking.getExitTime(), 
             entryTime, exitTime)) {
             return null; // Overlapping booking exists
         }
     }
     
     // Create new booking
     Booking booking = new Booking(user, parkingSlot, vehicleType,entryTime, exitTime);
     
     // Mark slot as occupied
     parkingSlotService.updateSlotAvailability(parkingSlot.getId(), false);
     
     return bookingRepository.save(booking);
 }
 
 private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, 
                             LocalDateTime start2, LocalDateTime end2) {
     return start1.isBefore(end2) && start2.isBefore(end1);
 }
 
 public Booking updateBookingStatus(Long bookingId, String status) {
     Booking booking = getBookingById(bookingId);
     if (booking != null) {
         booking.setStatus(status);
         return bookingRepository.save(booking);
     }
     return null;
 }
 
 public void cancelBooking(Long bookingId) {
     Booking booking = getBookingById(bookingId);
     if (booking != null && "BOOKED".equals(booking.getStatus())) {
         booking.setStatus("CANCELLED");
         bookingRepository.save(booking);
         
         // Make the slot available again
         parkingSlotService.updateSlotAvailability(booking.getParkingSlot().getId(), true);
     }
 }
 
 public long getTotalBookingsCount() {
    return bookingRepository.count();
 }
 
 public long getTodayBookingsCount() {
     LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
     LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
     
     return bookingRepository.findAll().stream()
         .filter(booking -> booking.getEntryTime().isAfter(startOfDay) && 
                         booking.getEntryTime().isBefore(endOfDay))
         .count();
 }

    public Booking saveBooking(Booking booking) {
    return bookingRepository.save(booking);
    }

    public BookingResponseDTO mapToDTO(Booking booking) {
    BookingResponseDTO dto = new BookingResponseDTO();
    dto.setId(booking.getId());
    dto.setUser(new UserDTO(booking.getUser()));  // your existing UserDTO constructor excludes password
    dto.setParkingSlot(new ParkingSlotDTO(booking.getParkingSlot()));

    dto.setVehicleType(booking.getVehicleType());
    dto.setEntryTime(booking.getEntryTime());
    dto.setExitTime(booking.getExitTime());
    dto.setTotalAmount(booking.getTotalAmount());
    dto.setStatus(booking.getStatus());
    dto.setPayment(booking.getPayment() != null ? new PaymentDTO(booking.getPayment()) : null);
    return dto;
}

    // 🎯 ENHANCED DEBUG SCHEDULER - Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateExpiredBookingsAndFreeSlots() {
        // Get times in multiple ways
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime utcNow = LocalDateTime.now(ZoneOffset.UTC);
        Instant instant = Instant.now();
        
        System.out.println("🕒 TIMEZONE DEBUG:");
        System.out.println("   Local Now: " + now);
        System.out.println("   UTC Now: " + utcNow);
        System.out.println("   Instant: " + instant);
        System.out.println("   JVM Timezone: " + TimeZone.getDefault().getID());
        System.out.println("   System Timezone: " + System.getProperty("user.timezone"));
        
        List<String> activeStatuses = List.of("BOOKED", "PAID");
        List<Booking> expiredBookings = bookingRepository.findByStatusInAndExitTimeBefore(activeStatuses, now);
        
        System.out.println("🔍 Found " + expiredBookings.size() + " expired bookings (LOCAL time comparison)");
        
        // Also try with UTC time
        List<Booking> expiredBookingsUTC = bookingRepository.findByStatusInAndExitTimeBefore(activeStatuses, utcNow);
        System.out.println("🔍 Found " + expiredBookingsUTC.size() + " expired bookings (UTC time comparison)");
        
        // Debug first few bookings
        List<Booking> allBookings = bookingRepository.findAll();
        System.out.println("🔍 Total bookings in database: " + allBookings.size());
        
        allBookings.stream()
            .filter(b -> activeStatuses.contains(b.getStatus()))
            .limit(3)
            .forEach(booking -> {
                System.out.println("📋 Booking #" + booking.getId() + 
                                 " | Status: " + booking.getStatus() + 
                                 " | Exit: " + booking.getExitTime() +
                                 " | Expired (Local): " + (booking.getExitTime() != null && booking.getExitTime().isBefore(now)) +
                                 " | Expired (UTC): " + (booking.getExitTime() != null && booking.getExitTime().isBefore(utcNow)));
            });

        // Process expired bookings (try both approaches)
        List<Booking> toProcess = expiredBookings.isEmpty() ? expiredBookingsUTC : expiredBookings;
        System.out.println("🔧 Processing " + toProcess.size() + " bookings...");
        
        for (Booking booking : toProcess) {
            try {
                String oldStatus = booking.getStatus();
                booking.setStatus("COMPLETED");
                bookingRepository.save(booking);
                parkingSlotService.updateSlotAvailability(booking.getParkingSlot().getId(), true);
                System.out.println("✅ Freed slot: " + booking.getParkingSlot().getSlotNumber() + 
                                 " (Booking #" + booking.getId() + " changed from " + oldStatus + " to COMPLETED)");
            } catch (Exception e) {
                System.err.println("❌ Error processing booking #" + booking.getId() + ": " + e.getMessage());
            }
        }
        
        if (toProcess.size() > 0) {
            System.out.println("🎉 Booking expiry check completed - processed " + toProcess.size() + " bookings");
        }
    }
}
