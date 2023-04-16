package com.example.carpark.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${carPark.costPerHour}")
    private double costPerHour;

    @Override
    public double calculateParkingCost(LocalDateTime arrived, LocalDateTime left) {
        int parkingDurationHours = calculateParkingDurationRoundedToNearestHour(arrived, left);
        return parkingDurationHours * costPerHour;
    }

    @Override
    public long calculateParkingDurationSecondsRounded(LocalDateTime arrived, LocalDateTime left) {
        long parkingDurationSeconds = Duration.between(arrived, left).getSeconds();
        // Found during testing (on occasion) parking was less than one second - round this up.
        if (parkingDurationSeconds < 1) {
            return 1;
        }
        return parkingDurationSeconds;
    }

    private int calculateParkingDurationRoundedToNearestHour(LocalDateTime arrived, LocalDateTime left) {
        long parkingDurationSeconds = calculateParkingDurationSecondsRounded(arrived, left);
        return (int) Math.ceil((double)parkingDurationSeconds/3600);
    }
}