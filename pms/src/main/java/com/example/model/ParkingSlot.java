package com.example.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false, unique = true)
 private String slotNumber;
 
 @Column(nullable = false)
 private String vehicleType; // "TWO_WHEELER", "THREE_WHEELER", "FOUR_WHEELER"
 
 @Column(nullable = false)
 private boolean isAvailable;
 
 @Column(nullable = false)
 private double hourlyRate;
 
 @OneToMany(mappedBy = "parkingSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 @JsonIgnoreProperties({"parkingSlot"})
 private List<Booking> bookings;
 
 // Default constructor
 public ParkingSlot() {
     this.isAvailable = true;
 }
 
 // Constructor with parameters
 public ParkingSlot(String slotNumber, String vehicleType, double hourlyRate) {
     this.slotNumber = slotNumber;
     this.vehicleType = vehicleType;
     this.hourlyRate = hourlyRate;
     this.isAvailable = true;
 }
 
 // Getters and Setters
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

 public boolean isAvailable() {
     return isAvailable;
 }

 public void setAvailable(boolean available) {
     isAvailable = available;
 }

 public double getHourlyRate() {
     return hourlyRate;
 }

 public void setHourlyRate(double hourlyRate) {
     this.hourlyRate = hourlyRate;
 }

 public List<Booking> getBookings() {
     return bookings;
 }

 public void setBookings(List<Booking> bookings) {
     this.bookings = bookings;
 }



}