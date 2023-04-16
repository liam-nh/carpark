package com.example.carpark.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParkingSpace {

    private int designation;

    private boolean evChargingPoint;

    private Vehicle occupyingVehicle;

    private LocalDateTime arrival;

    public ParkingSpace(int designation, boolean evChargingPoint) {
        this.designation = designation;
        this.evChargingPoint = evChargingPoint;
    }
}
