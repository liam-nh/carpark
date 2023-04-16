package com.example.carpark.exception;

public class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
