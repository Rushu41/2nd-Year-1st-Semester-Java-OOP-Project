package com.example.carrentalsystem;

import java.time.LocalDate;

public class RentalData {
    private String carName;
    private boolean isHourly;
    private String hours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String totalCost;

    public RentalData(String carName, boolean isHourly, String hours,
                      LocalDate startDate, LocalDate endDate, String totalCost) {
        this.carName = carName;
        this.isHourly = isHourly;
        this.hours = hours;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
    }

    // Add getters for all fields
    public String getCarName() { return carName; }
    public boolean isHourly() { return isHourly; }
    public String getHours() { return hours; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getTotalCost() { return totalCost; }
}