package com.example.dto;

import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private UserDTO user;           // Embedded user info
    private ParkingSlotDTO parkingSlot;
    private PaymentDTO payment;
    private String slot;            // Parking slot number
    private String vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double totalAmount;
    private String status;

    public BookingResponseDTO() {}

    public PaymentDTO getPayment() {
        return payment;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }
// Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public ParkingSlotDTO getParkingSlot() {
        return parkingSlot;
    }

    public void setParkingSlot(ParkingSlotDTO parkingSlot) {
        this.parkingSlot = parkingSlot;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
