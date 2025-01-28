package com.example.carrentalsystem;

public class SessionData {
    private static String selectedCarName;
    private static String customerName;
    private static boolean paymentCompleted = false;
    private static boolean receiptGenerated = false;

    public static String getSelectedCarName() {
        return selectedCarName;
    }

    public static void setSelectedCarName(String carName) {
        selectedCarName = carName;
    }

    public static String getCustomerName() {
        return customerName;
    }

    public static void setCustomerName(String name) {
        customerName = name;
    }

    public static boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public static void setPaymentCompleted(boolean completed) {
        paymentCompleted = completed;
    }

    public static boolean isReceiptGenerated() {
        return receiptGenerated;
    }

    public static void setReceiptGenerated(boolean generated) {
        receiptGenerated = generated;
    }
}
