package com.example.dto;

import com.example.model.ParkingSlot;

public class ParkingSlotDTO {
    private Long id;
    private String slotNumber;
    private String vehicleType;
    private Double hourlyRate;
    private Boolean available;

    public ParkingSlotDTO(ParkingSlot slot) {
        this.id = slot.getId();
        this.slotNumber = slot.getSlotNumber();
        this.vehicleType = slot.getVehicleType();
        this.hourlyRate = slot.getHourlyRate();
        this.available = slot.isAvailable();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

}
