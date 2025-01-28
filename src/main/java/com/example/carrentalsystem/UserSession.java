package com.example.carrentalsystem;

public class UserSession {
    private static String loggedInUsername;
    private static RentalData currentRental;
    private static boolean returningFromPayment;

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setCurrentRental(RentalData rental) {
        currentRental = rental;
    }

    public static RentalData getCurrentRental() {
        return currentRental;
    }

    public static void setReturningFromPayment(boolean value) {
        returningFromPayment = value;
    }

    public static boolean isReturningFromPayment() {
        return returningFromPayment;
    }

    public static void clearSession() {
        loggedInUsername = null;
        currentRental = null;
        returningFromPayment = false;
    }
}