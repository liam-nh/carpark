package com.example.carpark.util;

import com.example.carpark.model.ParkingSpace;
import com.example.carpark.model.Vehicle;
import org.instancio.Instancio;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class CarParkDataGenerator {

    private Random random = new Random();

    public List<ParkingSpace> generateParkingSpaces(int capacity) {
        List<ParkingSpace> parkingSpaces = Collections.synchronizedList(new ArrayList<>());

        for (int i=1; i<=capacity; i++) {
            parkingSpaces.add(new ParkingSpace(i, random.nextBoolean()));
        }

        return parkingSpaces;
    }

    public List<Vehicle> generateRandomVehicles(int numVehicles) {
        List<Vehicle> allVehicles = Collections.synchronizedList(new ArrayList<>());

        for (int i=1; i<=numVehicles; i++) {
            allVehicles.add(Instancio.create(Vehicle.class));
        }

        return allVehicles;
    }
}
