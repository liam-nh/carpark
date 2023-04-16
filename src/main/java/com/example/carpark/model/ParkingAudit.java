package com.example.carpark.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ParkingAudit {
    private int parkingSpaceDesignation;

    private String vehicleVrm;

    private LocalDateTime arrived;

    private LocalDateTime left;

    private double parkingCharge;

    /* TODO: Currently we only audit when a car leaves the car park (capturing their full duration, parking charge, etc).
     * Audit functionality could be reworked to have specific audit types (enter car park, leave car park, tried to
     * enter car park but was full, parking space out of use, etc.) audited against specific vehicles and/or parking
     * spaces at a given date time. */
}
