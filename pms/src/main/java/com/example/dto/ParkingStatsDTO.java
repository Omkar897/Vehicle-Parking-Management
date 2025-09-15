package com.example.dto;

import com.example.model.ParkingSlot;

public class ParkingStatsDTO {
    private int totalSlots;
    private int availableSlots;
    private VehicleTypeStats twoWheelers;
    private VehicleTypeStats threeWheelers;
    private VehicleTypeStats fourWheelers;
    private VehicleTypeStats evWheelers;



    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public VehicleTypeStats getTwoWheelers() {
        return twoWheelers;
    }

    public void setTwoWheelers(VehicleTypeStats twoWheelers) {
        this.twoWheelers = twoWheelers;
    }

    public VehicleTypeStats getThreeWheelers() {
        return threeWheelers;
    }

    public void setThreeWheelers(VehicleTypeStats threeWheelers) {
        this.threeWheelers = threeWheelers;
    }

    public VehicleTypeStats getFourWheelers() {
        return fourWheelers;
    }

    public void setFourWheelers(VehicleTypeStats fourWheelers) {
        this.fourWheelers = fourWheelers;
    }

    public VehicleTypeStats getEvWheelers() {
        return evWheelers;
    }

    public void setEvWheelers(VehicleTypeStats evWheelers) {
        this.evWheelers = evWheelers;
    }

    public ParkingStatsDTO() {
        // initializes default values if needed
    }

    public static class VehicleTypeStats {
        private int used;
        private int total;
        private int available;

        public int getUsed() {
            return used;
        }

        public int getAvailable() {
            return available;
        }

        public void setAvailable(int available) {
            this.available = available;
        }

        public void setUsed(int used) {
            this.used = used;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}
