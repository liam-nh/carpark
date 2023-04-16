package com.example.carpark.exception;

public class CarParkFullException extends Exception {
    public CarParkFullException(String errorMessage) {
        super(errorMessage);
    }
}
