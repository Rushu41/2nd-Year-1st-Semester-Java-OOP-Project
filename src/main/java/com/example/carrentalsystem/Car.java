package com.example.carrentalsystem;

public class Car {
    private String name;         // Car name
    private int totalSeats;      // Number of seats in the car
    private String fuelType;     // Type of fuel (e.g., Petrol, Diesel)
    private double rentPricePerDay; // Daily rental price
    private String customerName; // Customer name for rented cars
    private String photoPath;    // Path to the car's photo

    // Constructor for available cars
    public Car(String name, int totalSeats, String fuelType, double rentPricePerDay) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.fuelType = fuelType;
        this.rentPricePerDay = rentPricePerDay;
    }

    // Constructor for rented cars
    public Car(String name, String type, double rentPricePerDay, String customerName) {
        this.name = name;
        this.fuelType = type; // Alias `fuelType` as `type` in query
        this.rentPricePerDay = rentPricePerDay;
        this.customerName = customerName;
    }

    // Constructor with all fields
    public Car(String name, String type, int totalSeats, String fuelType, double rentPricePerDay, String customerName) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.fuelType = fuelType;
        this.rentPricePerDay = rentPricePerDay;
        this.customerName = customerName;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getRentPricePerDay() {
        return rentPricePerDay;
    }

    public void setRentPricePerDay(double rentPricePerDay) {
        this.rentPricePerDay = rentPricePerDay;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Getter for photoPath
    public String getPhotoPath() {
        return photoPath;
    }

    // Setter for photoPath
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}