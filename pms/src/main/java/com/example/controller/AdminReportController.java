package com.example.controller;

import com.example.dto.AdminStatsDTO;
import com.example.service.BookingService;
import com.example.service.ParkingSlotService;
import com.example.service.PaymentService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private PaymentService paymentService;

    // Endpoint for dashboard summary stats
    @GetMapping("/summary")
    public AdminStatsDTO getAdminSummary() {
        AdminStatsDTO stats = new AdminStatsDTO();

        stats.setTodayBookings((int) bookingService.getTodayBookingsCount());
        stats.setUserCount((int) userService.countUsers());
        stats.setTotalBookings((int) bookingService.getTotalBookingsCount());
        stats.setTotalSlots((int) parkingSlotService.getTotalSlots());
        stats.setAvailableSlots((int) parkingSlotService.getAvailableSlotsCount());
        stats.setOccupiedSlots((int) parkingSlotService.getOccupiedSlotsCount());
        stats.setTotalRevenue(paymentService.getTotalSuccessfulRevenue());

        return stats;
    }
}
