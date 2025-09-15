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

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ParkingSlotService parkingSlotService;

    public List<Booking> getAllBookings() {
        updateExpiredBookingsAndFreeSlots(); // update statuses before fetching
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

    public Booking createBooking(User user, ParkingSlot parkingSlot, String vehicleType, LocalDateTime entryTime,
            LocalDateTime exitTime) {
        // Check if the slot is available
        if (!parkingSlot.isAvailable()) {
            return null;
        }

        // Check for overlapping bookings - get all bookings for this slot except
        // cancelled ones
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
        Booking booking = new Booking(user, parkingSlot, vehicleType, entryTime, exitTime);

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
        dto.setUser(new UserDTO(booking.getUser())); // your existing UserDTO constructor excludes password
        dto.setParkingSlot(new ParkingSlotDTO(booking.getParkingSlot()));

        dto.setVehicleType(booking.getVehicleType());
        dto.setEntryTime(booking.getEntryTime());
        dto.setExitTime(booking.getExitTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setPayment(booking.getPayment() != null ? new PaymentDTO(booking.getPayment()) : null);
        return dto;
    }

    // 🎯 FIXED: Now runs automatically every 1 minute
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds (1 minute)
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void testScheduler() {
        System.out.println("🔍 TEST SCHEDULER RUNNING at " + LocalDateTime.now());
    }

    @Transactional
    public void updateExpiredBookingsAndFreeSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<String> activeStatuses = List.of("BOOKED", "PAID");

        List<Booking> expiredBookings = bookingRepository.findByStatusInAndExitTimeBefore(activeStatuses, now);

        for (Booking booking : expiredBookings) {
            booking.setStatus("COMPLETED"); // or another status indicating booking ended
            bookingRepository.save(booking);
            parkingSlotService.updateSlotAvailability(booking.getParkingSlot().getId(), true);

            // Add logging to see it working
            System.out.println("✅ Expired booking freed: Slot " + booking.getParkingSlot().getSlotNumber() +
                    " (Booking ID: " + booking.getId() + ") at " + LocalDateTime.now());
        }

        if (!expiredBookings.isEmpty()) {
            System.out
                    .println("🔄 Processed " + expiredBookings.size() + " expired bookings at " + LocalDateTime.now());
        }
    }
}
