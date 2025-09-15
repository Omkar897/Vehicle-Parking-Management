package com.example.dto;

public class AdminStatsDTO {
    private int userCount;
    private int todayBookings;
    private int totalBookings;
    private int totalSlots;
    private int availableSlots;
    private int occupiedSlots;
    private double totalRevenue; // or `long` if you prefer

    // Getters and setters
    public int getUserCount() { return userCount; }
    public void setUserCount(int c) { userCount = c; }
    public int getTodayBookings() { return todayBookings; }
    public void setTodayBookings(int c) { todayBookings = c; }
    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int c) { totalBookings = c; }
    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int c) { totalSlots = c; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int c) { availableSlots = c; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(int c) { occupiedSlots = c; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double d) { totalRevenue = d; }
}
