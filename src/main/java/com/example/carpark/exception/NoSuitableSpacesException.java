package com.example.carpark.exception;

public class NoSuitableSpacesException extends Exception {
    public NoSuitableSpacesException(String errorMessage) {
        super(errorMessage);
    }
}
