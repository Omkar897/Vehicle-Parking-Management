package com.example.service;

import com.example.dto.ParkingStatsDTO;
import com.example.model.ParkingSlot;
import com.example.model.User;
import com.example.model.Booking;
import com.example.model.Payment;

import com.example.repository.ParkingSlotRepository;
import com.example.repository.BookingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingSlotService {

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public List<ParkingSlot> getAllSlots() {
        return parkingSlotRepository.findAll();
    }

    public List<ParkingSlot> getAvailableSlots() {
        return parkingSlotRepository.findByIsAvailableTrue();
    }

    public List<ParkingSlot> getAvailableSlotsByType(String vehicleType) {
        return parkingSlotRepository.findByIsAvailableTrueAndVehicleType(vehicleType);
    }

    public ParkingSlot getSlotById(Long id) {
        Optional<ParkingSlot> slot = parkingSlotRepository.findById(id);
        return slot.orElse(null);
    }

    public ParkingSlot getSlotByNumber(String slotNumber) {
        return parkingSlotRepository.findBySlotNumber(slotNumber);
    }

    public ParkingSlot saveSlot(ParkingSlot slot) {
        return parkingSlotRepository.save(slot);
    }

    public void updateSlotAvailability(Long slotId, boolean isAvailable) {
        ParkingSlot slot = getSlotById(slotId);
        if (slot != null) {
            // Update slot availability
            slot.setAvailable(isAvailable);
            parkingSlotRepository.save(slot);

            // If admin marks slot as available, complete any active bookings
            if (isAvailable) {
                // Find all active bookings for this slot
                List<Booking> activeBookings = bookingRepository.findAll().stream()
                        .filter(booking -> {
                            return booking.getParkingSlot() != null &&
                                    booking.getParkingSlot().getId().equals(slotId) &&
                                    ("BOOKED".equals(booking.getStatus()) || "PAID".equals(booking.getStatus()));
                        })
                        .collect(java.util.stream.Collectors.toList());

                // Complete all active bookings
                for (Booking booking : activeBookings) {
                    booking.setStatus("COMPLETED");
                    bookingRepository.save(booking);
                    System.out.println("✅ Admin override: Completed booking #" + booking.getId() +
                            " for slot " + slot.getSlotNumber());
                }

                if (activeBookings.size() > 0) {
                    System.out.println("🎉 Admin freed slot " + slot.getSlotNumber() +
                            ". Completed " + activeBookings.size() + " active bookings.");
                } else {
                    System.out.println(
                            "✅ Slot " + slot.getSlotNumber() + " marked as available (no active bookings found).");
                }
            } else {
                System.out.println("✅ Slot " + slot.getSlotNumber() + " marked as occupied.");
            }
        } else {
            System.err.println("❌ Slot with ID " + slotId + " not found!");
        }
    }

    public long getTotalSlots() {
        return parkingSlotRepository.count();
    }

    public long getAvailableSlotsCount() {
        return parkingSlotRepository.findByIsAvailableTrue().size();
    }

    public long getOccupiedSlotsCount() {
        return getTotalSlots() - getAvailableSlotsCount();
    }

    public long getTwoWheelerSlotsCount() {
        return parkingSlotRepository.findByVehicleType("TWO_WHEELER").size();
    }

    public long getThreeWheelerSlotsCount() {
        return parkingSlotRepository.findByVehicleType("THREE_WHEELER").size();
    }

    public long getFourWheelerSlotsCount() {
        return parkingSlotRepository.findByVehicleType("FOUR_WHEELER").size();
    }

    public long getAvailableTwoWheelerSlotsCount() {
        return parkingSlotRepository.findByIsAvailableTrueAndVehicleType("TWO_WHEELER").size();
    }

    public long getAvailableThreeWheelerSlotsCount() {
        return parkingSlotRepository.findByIsAvailableTrueAndVehicleType("THREE_WHEELER").size();
    }

    public long getAvailableFourWheelerSlotsCount() {
        return parkingSlotRepository.findByIsAvailableTrueAndVehicleType("FOUR_WHEELER").size();
    }

    public long getEVSlotsCount() {
        return parkingSlotRepository.findByVehicleType("EV").size();
    }

    public long getAvailableEVSlotsCount() {
        return parkingSlotRepository.findByIsAvailableTrueAndVehicleType("EV").size();
    }

    // Add these methods that were referenced but not defined
    public List<User> getAllUsers() {
        // This would be implemented in UserService, not here
        return null;
    }

    public void deleteSlot(Long id) {
        parkingSlotRepository.deleteById(id);
    }

    public ParkingSlot findById(Long id) {
        return parkingSlotRepository.findById(id).orElse(null);
    }

    public List<Payment> getAllPayments() {
        // This would be implemented in PaymentService, not here
        return null;
    }

    public List<Booking> getActiveBookingsForSlot(Long slotId) {
        return bookingRepository.findByParkingSlotIdAndStatus(slotId, "BOOKED");
    }

    public ParkingStatsDTO getParkingStats() {
        ParkingStatsDTO stats = new ParkingStatsDTO();

        // Total slots count (all slots)
        long totalSlots = parkingSlotRepository.count();

        // Count of slots currently booked (active bookings)
        long bookedSlots = bookingRepository.countActiveBookings();

        // Count of slots marked available (is_available = true)
        long slotsMarkedAvailable = parkingSlotRepository.countByIsAvailableTrue();

        // Calculate real available slots as slots marked available minus booked slots
        long availableSlots = slotsMarkedAvailable;

        // Vehicle type totals
        long totalTwo = parkingSlotRepository.countByVehicleType("TWO_WHEELER");
        long totalThree = parkingSlotRepository.countByVehicleType("THREE_WHEELER");
        long totalFour = parkingSlotRepository.countByVehicleType("FOUR_WHEELER");
        long totalEV = parkingSlotRepository.countByVehicleType("EV");

        long availableTwo = parkingSlotRepository.countByIsAvailableTrueAndVehicleType("TWO_WHEELER");
        long availableThree = parkingSlotRepository.countByIsAvailableTrueAndVehicleType("THREE_WHEELER");
        long availableFour = parkingSlotRepository.countByIsAvailableTrueAndVehicleType("FOUR_WHEELER");
        long availableEV = parkingSlotRepository.countByIsAvailableTrueAndVehicleType("EV");

        long usedTwo = totalTwo - availableTwo;
        long usedThree = totalThree - availableThree;
        long usedFour = totalFour - availableFour;
        long usedEV = totalEV - availableEV;

        // Debug logs for tracking counts
        System.out.println("Total slots: " + totalSlots);
        System.out.println("Booked slots: " + bookedSlots);
        System.out.println("Slots marked available: " + slotsMarkedAvailable);
        System.out.println("Calculated available slots: " + availableSlots);

        // Set total and available slots
        stats.setTotalSlots((int) totalSlots);
        stats.setAvailableSlots((int) availableSlots);

        ParkingStatsDTO.VehicleTypeStats twoStats = new ParkingStatsDTO.VehicleTypeStats();
        twoStats.setTotal((int) totalTwo);
        twoStats.setUsed((int) usedTwo);
        twoStats.setAvailable((int) availableTwo);

        ParkingStatsDTO.VehicleTypeStats threeStats = new ParkingStatsDTO.VehicleTypeStats();
        threeStats.setTotal((int) totalThree);
        threeStats.setUsed((int) usedThree);
        threeStats.setAvailable((int) availableThree);

        ParkingStatsDTO.VehicleTypeStats fourStats = new ParkingStatsDTO.VehicleTypeStats();
        fourStats.setTotal((int) totalFour);
        fourStats.setUsed((int) usedFour);
        fourStats.setAvailable((int) availableFour);

        ParkingStatsDTO.VehicleTypeStats evStats = new ParkingStatsDTO.VehicleTypeStats();
        evStats.setTotal((int) totalEV);
        evStats.setUsed((int) usedEV);
        evStats.setAvailable((int) availableEV);

        stats.setTwoWheelers(twoStats);
        stats.setThreeWheelers(threeStats);
        stats.setFourWheelers(fourStats);
        stats.setEvWheelers(evStats);

        return stats;
    }

}