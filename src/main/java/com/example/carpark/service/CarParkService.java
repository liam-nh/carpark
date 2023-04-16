package com.example.carpark.service;

import com.example.carpark.exception.CarParkFullException;
import com.example.carpark.exception.DoubleParkedException;
import com.example.carpark.exception.NoSuitableSpacesException;
import com.example.carpark.exception.VehicleNotFoundException;
import com.example.carpark.model.ParkingSpace;
import com.example.carpark.model.Vehicle;

public interface CarParkService {

    /**
     * Attempts to park a supplied vehicle in an initialised car park.
     * @param vehicle the vehicle to be parked.
     * @return the allocated parking space if parking was successful, otherwise null.
     * @throws DoubleParkedException if the supplied vehicle is already parked in the car park.
     * @throws CarParkFullException if the car park has reached its maximum capacity.
     * @throws NoSuitableSpacesException if the car park does not have a space suitable for the supplied vehicle.
     * Note: we assume an EV requires an EV charging parking space and a non-EV vehicle cannot use an EV parking space.
     */
    ParkingSpace parkVehicle(Vehicle vehicle) throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException;

    /**
     * Removes a parked vehicle from the car park, thus freeing up the parking space it was using.
     * @param vehicle the parked vehicle leaving the car park.
     * @return the parking charge amount (rounded to the nearest hour).
     * Note: the minimum charge if parked for <1 hour is the hourly rate.
     * @throws VehicleNotFoundException if the supplied vehicle is not parked in the car park.
     */
    double leaveCarPark(Vehicle vehicle) throws VehicleNotFoundException;

    /**
     * Checks whether suitable parking is available in the car park for the supplied vehicle.
     * @param vehicle the vehicle enquiring about parking.
     * @return true if the car park has a parking space suitable for the supplied vehicle (i.e. we return true for an EV
     * if an EV charging parking space is available), otherwise false.
     */
    boolean isSuitableParkingAvailable(Vehicle vehicle);

    /**
     * Returns true if the car park is full (number of vehicles parked = maximum capacity).
     * @return true if the car park has reached maximum capacity, otherwise false.
     */
    boolean isCarParkFull();

    /**
     * Returns the number of vehicles currently residing in the car park.
     * @return the number of vehicles currently parked in the car park.
     */
    int getNumCarsParked();

    /**
     * Returns a random vehicle from the selection of vehicles currently residing in the car park.
     * @return a vehicle at random from the collection of vehicle currently parked in the car park.
     */
    Vehicle getRandomlyParkedVehicle();
}