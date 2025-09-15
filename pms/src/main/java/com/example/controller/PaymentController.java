package com.example.controller;

import com.example.model.Booking;
import com.example.model.Payment;
import com.example.service.BookingService;
import com.example.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody Map<String, String> paymentData) {
        try {
            Long bookingId = Long.valueOf(paymentData.get("bookingId"));
            String paymentMethod = paymentData.get("paymentMethod");

            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found"));
            }

            Payment payment = paymentService.processPayment(booking, paymentMethod);
            booking.setStatus("PAID");
            bookingService.saveBooking(booking);


            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
