package com.example.repository;

import com.example.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
 List<ParkingSlot> findByIsAvailableTrue();
 List<ParkingSlot> findByVehicleType(String vehicleType);
 List<ParkingSlot> findByIsAvailableTrueAndVehicleType(String vehicleType);
 ParkingSlot findBySlotNumber(String slotNumber);

    // Count all slots
    long count();

    // Count slots where isAvailable is true
    long countByIsAvailableTrue();

    // Count slots by vehicle type
    long countByVehicleType(String vehicleType);

    // Count slots by vehicle type and availability true
    long countByIsAvailableTrueAndVehicleType(String vehicleType);
}