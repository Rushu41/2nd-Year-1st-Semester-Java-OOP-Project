package com.example.carrentalsystem;

public class Rental {
    private int id;
    private String carName;
    private String customerName;
    private String startDate;
    private String endDate;
    private long rentalDays;

    public Rental(int id, String carName, String customerName, String startDate, String endDate, long rentalDays) {
        this.id = id;
        this.carName = carName;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalDays = rentalDays;
    }

    public int getId() {
        return id;
    }

    public String getCarName() {
        return carName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public long getRentalDays() {
        return rentalDays;
    }
}
