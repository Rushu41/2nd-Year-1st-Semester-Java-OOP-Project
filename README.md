# Java-OOP-Project

# JavaFX Car Rental System

## Overview
This is a JavaFX-based Car Rental System that provides functionalities for both customers and administrators. The system allows users to rent cars, make payments, and manage rental transactions.

## Setup Instructions

### Prerequisites
Before running the project, ensure you have the following installed:
- Java Development Kit (JDK) 8 or later
- MySQL Database
- JavaFX SDK (if required separately)
- A compatible IDE (e.g., IntelliJ IDEA, Eclipse, NetBeans)

### Database Setup
1. Open MySQL and create a new database.
2. Run the SQL script provided in sql_tables.txt to create the necessary tables.
3. Note down the database connection details (host, port, database name, username, password).

### Configuration
You must manually insert your MySQL database details in the following Java classes:
- CreditCardDetailsController.java
- DatabaseConnector.java
- PaymentController.java
- RentCarPaymentController.java

#### Example Configuration in DatabaseConnector.java:
private static final String URL = "jdbc:mysql://localhost:3306/your_database_name";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
Replace your_database_name, your_username, and your_password with your actual MySQL credentials.

## Running the Application
1. Open the project in your IDE.
2. Ensure JavaFX is correctly configured.
3. Compile and run the application.

## Features
- Customer Registration and Login
- Car Rental Booking System
- Payment Processing
- Admin Panel for Rental Management

## Troubleshooting
- If the database connection fails, verify that MySQL is running and the credentials are correctly set.
- Ensure the sql_tables.txt script has been executed successfully.

## License
This project is open-source and available for modification as per the licensing terms.