package com.example.carpark.service;

import java.time.LocalDateTime;

public interface PaymentService {

    /**
     * Calculates the parking cost (rounded up to the nearest hour) for a supplied car park arrival and departure time.
     * @param arrived when the vehicle first occupied the parking space.
     * @param left when the vehicle left the parking space.
     * @return the parking charge amount (in pounds).
     */
    double calculateParkingCost(LocalDateTime arrived, LocalDateTime left);

    /**
     * Returns the parking duration rounded to the nearest second for a supplied car park arrival and departure time.
     * @param arrived when the vehicle first occupied the parking space.
     * @param left when the vehicle left the parking space.
     * @return the number of seconds spent in the car park (rounded to the nearest second).
     */
    long calculateParkingDurationSecondsRounded(LocalDateTime arrived, LocalDateTime left);

    // TODO: Possible future extension: this service could be further extended to handle actual payment (cash/card/etc).
}
