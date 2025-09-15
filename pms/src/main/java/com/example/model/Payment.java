package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @OneToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "booking_id", nullable = false)
 private Booking booking;
 
 @Column(nullable = false)
 private double amount;
 
 @Column(nullable = false)
 private String paymentMethod; // "CREDIT_CARD", "DEBIT_CARD", "UPI", "CASH"
 
 @Column(nullable = false)
 private String transactionId;
 
 @Column(nullable = false)
 private LocalDateTime paymentDate;
 
 @Column(nullable = false)
 private String status; // "SUCCESS", "FAILED", "PENDING"
 
 // Default constructor
 public Payment() {
     this.paymentDate = LocalDateTime.now();
     this.status = "PENDING";
 }
 
 // Constructor with parameters
 public Payment(Booking booking, double amount, String paymentMethod, String transactionId) {
     this.booking = booking;
     this.amount = amount;
     this.paymentMethod = paymentMethod;
     this.transactionId = transactionId;
     this.paymentDate = LocalDateTime.now();
     this.status = "SUCCESS";
 }
 
 // Getters and Setters
 public Long getId() {
     return id;
 }

 public void setId(Long id) {
     this.id = id;
 }

 public Booking getBooking() {
     return booking;
 }

 public void setBooking(Booking booking) {
     this.booking = booking;
 }

 public double getAmount() {
     return amount;
 }

 public void setAmount(double amount) {
     this.amount = amount;
 }

 public String getPaymentMethod() {
     return paymentMethod;
 }

 public void setPaymentMethod(String paymentMethod) {
     this.paymentMethod = paymentMethod;
 }

 public String getTransactionId() {
     return transactionId;
 }

 public void setTransactionId(String transactionId) {
     this.transactionId = transactionId;
 }

 public LocalDateTime getPaymentDate() {
     return paymentDate;
 }

 public void setPaymentDate(LocalDateTime paymentDate) {
     this.paymentDate = paymentDate;
 }

 public String getStatus() {
     return status;
 }

 public void setStatus(String status) {
     this.status = status;
 }
}