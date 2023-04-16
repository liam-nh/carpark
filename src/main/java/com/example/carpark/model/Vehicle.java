package com.example.carpark.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Vehicle {

    private String vrm;

    private VehicleType vehicleType;

    public Vehicle(String vrm, VehicleType vehicleType) {
        this.vrm = vrm;
        this.vehicleType = vehicleType;
    }
}
