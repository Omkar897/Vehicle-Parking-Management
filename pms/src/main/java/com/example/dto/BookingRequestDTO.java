package com.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BookingRequestDTO {

    private String slotNumber;

    @JsonProperty("vehicleType") // ensures JSON "vehicleType" maps correctly
    private String vehicleType;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ensures LocalDateTime parses correctly
    private LocalDateTime entryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime exitTime;


    public BookingRequestDTO() {}

    public BookingRequestDTO(String slotNumber, String vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
    }

    // Getters and Setters
    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
}

