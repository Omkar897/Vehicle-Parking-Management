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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
 
 @Autowired
 private BookingRepository bookingRepository;
 
 @Autowired
 private ParkingSlotService parkingSlotService;

 @Transactional
 public List<Booking> getAllBookings() {
     updateExpiredBookingsAndFreeSlots();
     return bookingRepository.findAllWithUserAndSlot();
 }

 @Transactional
 public List<Booking> getUserBookings(User user) {
     updateExpiredBookingsAndFreeSlots();
     return bookingRepository.findByUser(user);
 }
 
 public Booking getBookingById(Long id) {
     Optional<Booking> booking = bookingRepository.findById(id);
     return booking.orElse(null);
 }
 
 @Transactional
 public Booking createBooking(User user, ParkingSlot parkingSlot,String vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
     if (!parkingSlot.isAvailable()) {
         return null;
     }
     
     List<Booking> existingBookings = bookingRepository.findByParkingSlotIdAndStatusNot(
         parkingSlot.getId(), "CANCELLED");
     
     for (Booking existingBooking : existingBookings) {
         if (isOverlapping(existingBooking.getEntryTime(), existingBooking.getExitTime(), 
             entryTime, exitTime)) {
             return null;
         }
     }
     
     Booking booking = new Booking(user, parkingSlot, vehicleType,entryTime, exitTime);
     parkingSlotService.updateSlotAvailability(parkingSlot.getId(), false);
     
     return bookingRepository.save(booking);
 }
 
 private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, 
                             LocalDateTime start2, LocalDateTime end2) {
     return start1.isBefore(end2) && start2.isBefore(end1);
 }
 
 @Transactional
 public Booking updateBookingStatus(Long bookingId, String status) {
     Booking booking = getBookingById(bookingId);
     if (booking != null) {
         booking.setStatus(status);
         return bookingRepository.save(booking);
     }
     return null;
 }
 
 @Transactional
 public void cancelBooking(Long bookingId) {
     Booking booking = getBookingById(bookingId);
     if (booking != null && "BOOKED".equals(booking.getStatus())) {
         booking.setStatus("CANCELLED");
         bookingRepository.save(booking);
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
    dto.setUser(new UserDTO(booking.getUser()));
    dto.setParkingSlot(new ParkingSlotDTO(booking.getParkingSlot()));

    dto.setVehicleType(booking.getVehicleType());
    dto.setEntryTime(booking.getEntryTime());
    dto.setExitTime(booking.getExitTime());
    dto.setTotalAmount(booking.getTotalAmount());
    dto.setStatus(booking.getStatus());
    dto.setPayment(booking.getPayment() != null ? new PaymentDTO(booking.getPayment()) : null);
    return dto;
 }

 // 🎯 FIXED VERSION - Uses simple findAll() instead of complex repository method
 @Scheduled(fixedRate = 60000) // Every 1 minute
 @Transactional
 public void updateExpiredBookingsAndFreeSlots() {
     LocalDateTime now = LocalDateTime.now();
     
     System.out.println("🔍 Expiry scheduler running at " + now);
     
     try {
         // Get ALL bookings and filter in Java (more reliable)
         List<Booking> allBookings = bookingRepository.findAll();
         
         List<Booking> expiredBookings = allBookings.stream()
             .filter(booking -> {
                 // Check if status is BOOKED or PAID
                 boolean isActiveStatus = "BOOKED".equals(booking.getStatus()) || "PAID".equals(booking.getStatus());
                 
                 // Check if exit time has passed
                 boolean isExpired = booking.getExitTime() != null && booking.getExitTime().isBefore(now);
                 
                 return isActiveStatus && isExpired;
             })
             .collect(Collectors.toList());

         System.out.println("🔍 Total bookings: " + allBookings.size());
         System.out.println("🔍 Found " + expiredBookings.size() + " expired bookings to process");

         int processedCount = 0;
         for (Booking booking : expiredBookings) {
             try {
                 String oldStatus = booking.getStatus();
                 booking.setStatus("COMPLETED");
                 bookingRepository.save(booking);
                 
                 // Free the parking slot
                 if (booking.getParkingSlot() != null) {
                     parkingSlotService.updateSlotAvailability(booking.getParkingSlot().getId(), true);
                     processedCount++;
                     
                     System.out.println("✅ Booking #" + booking.getId() + " expired: " +
                                      "Slot " + booking.getParkingSlot().getSlotNumber() + 
                                      " freed (was " + oldStatus + " -> COMPLETED)");
                 }
             } catch (Exception e) {
                 System.err.println("❌ Error processing booking #" + booking.getId() + ": " + e.getMessage());
             }
         }
         
         if (processedCount > 0) {
             System.out.println("🎉 Successfully processed " + processedCount + " expired bookings");
         }
         
     } catch (Exception e) {
         System.err.println("❌ Error in scheduler: " + e.getMessage());
         e.printStackTrace();
     }
 }
}
