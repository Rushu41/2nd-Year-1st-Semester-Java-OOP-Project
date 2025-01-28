package com.example.carrentalsystem;

public class RentalHistory {
    private String carName;
    private String carType;
    private String customerName;
    private String returnDate;

    public RentalHistory(String carName, String carType, String customerName, String returnDate) {
        this.carName = carName;
        this.carType = carType;
        this.customerName = customerName;
        this.returnDate = returnDate;
    }

    public String getCarName() {
        return carName;
    }

    public String getCarType() {
        return carType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getReturnDate() {
        return returnDate;
    }
}
