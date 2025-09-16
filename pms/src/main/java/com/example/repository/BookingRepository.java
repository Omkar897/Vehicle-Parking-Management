package com.example.repository;

import com.example.model.Booking;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByStatus(String status);
    List<Booking> findByParkingSlotIdAndStatusNot(Long parkingSlotId, String status);
    List<Booking> findByParkingSlotIdAndStatus(Long parkingSlotId, String status);
    List<Booking> findByStatusInAndExitTimeBefore(List<String> statuses, LocalDateTime time);

    // Count all active bookings (not cancelled and current time between entry and exit)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status <> 'CANCELLED' AND b.entryTime <= CURRENT_TIMESTAMP AND b.exitTime >= CURRENT_TIMESTAMP")
    long countActiveBookings();

    // Count active bookings filtered by vehicle type
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status <> 'CANCELLED' AND b.entryTime <= CURRENT_TIMESTAMP AND b.exitTime >= CURRENT_TIMESTAMP AND b.vehicleType = :vehicleType")
    long countActiveBookingsByVehicleType(@Param("vehicleType") String vehicleType);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.parkingSlot")
    List<Booking> findAllWithUserAndSlot();
    
    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses AND b.exitTime < :time")
    List<Booking> findExpiredBookings(@Param("statuses") List<String> statuses, @Param("time") LocalDateTime time);

    @Query("SELECT b FROM Booking b WHERE b.parkingSlot.id = :slotId " +
           "AND b.status IN :activeStatuses " +
           "AND ((b.entryTime < :exitTime) AND (b.exitTime > :entryTime))")
    List<Booking> findConflictingActiveBookings(@Param("slotId") Long slotId,
                                                @Param("entryTime") LocalDateTime entryTime,
                                                @Param("exitTime") LocalDateTime exitTime,
                                                @Param("activeStatuses") List<String> activeStatuses);
}
