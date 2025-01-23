package com.example.carrentalsystem;

public class CustomerData {
    private String username;
    private String carName;
    private String rentingStatus;
    private String returnCarStatus;
    private double bill;
    private int totalRenting;

    public CustomerData(String username, String carName, String rentingStatus, String returnCarStatus, double bill, int totalRenting) {
        this.username = username;
        this.carName = carName;
        this.rentingStatus = rentingStatus;
        this.returnCarStatus = returnCarStatus;
        this.bill = bill;
        this.totalRenting = totalRenting;
    }

    public String getUsername() {
        return username;
    }

    public String getCarName() {
        return carName;
    }

    public String getRentingStatus() {
        return rentingStatus;
    }

    public String getReturnCarStatus() {
        return returnCarStatus;
    }

    public double getBill() {
        return bill;
    }

    public int getTotalRenting() {
        return totalRenting;
    }
}
