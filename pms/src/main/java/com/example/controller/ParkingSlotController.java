package com.example.controller;

import com.example.dto.ParkingStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.model.ParkingSlot;
import com.example.service.ParkingSlotService;

@RestController
@RequestMapping("/api/parkingSlots")
public class ParkingSlotController {

    @Autowired
    private ParkingSlotService parkingSlotService;
    @GetMapping
    public List<ParkingSlot> getAvailableSlots(
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false, defaultValue = "true") boolean available) {

        if (vehicleType != null && available) {
            return parkingSlotService.getAvailableSlotsByType(vehicleType);
        } else if (available) {
            return parkingSlotService.getAvailableSlots();
        } else {
            return parkingSlotService.getAllSlots();
        }
    }

    // Add this method for stats endpoint
    @GetMapping("/stats")
    public ResponseEntity<ParkingStatsDTO> getParkingStats() {
        ParkingStatsDTO stats = parkingSlotService.getParkingStats();
        return ResponseEntity.ok(stats);
    }



}
