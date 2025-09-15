package com.example.service;

import com.example.model.Booking;
import com.example.model.Payment;
import com.example.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
 
 @Autowired
 private PaymentRepository paymentRepository;
 
 public Payment processPayment(Booking booking, String paymentMethod) {
     // Generate a unique transaction ID
     String transactionId = "TXN" + System.currentTimeMillis();
     
     Payment payment = new Payment(booking, booking.getTotalAmount(), paymentMethod, transactionId);
     return paymentRepository.save(payment);
 }
 
 public Payment getPaymentById(Long id) {
     Optional<Payment> payment = paymentRepository.findById(id);
     return payment.orElse(null);
 }
 
 public Payment getPaymentByTransactionId(String transactionId) {
     return paymentRepository.findByTransactionId(transactionId);
 }
 
 public Payment savePayment(Payment payment) {
     return paymentRepository.save(payment);
 }

 public List<Payment> getAllPayments() {
     return paymentRepository.findAll();
 }

    public double getTotalSuccessfulRevenue() {
        List<Payment> successfulPayments = paymentRepository.findByStatus("SUCCESS");
        return successfulPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}