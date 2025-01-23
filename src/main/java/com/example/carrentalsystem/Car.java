package com.example.carrentalsystem;

public class Car {
    private String name;
    private String type;
    private double price;
    private String customerName;

    public Car(String name, String type, double price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }
    public Car(String name, String type, double price, String customerName) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.customerName = customerName;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public String getCustomerName() { return customerName;}

    // Setters (optional if you need to update the car object)
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCustomerName() { this.customerName = customerName;}

}
