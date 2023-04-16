package com.example.carpark.service;

import com.example.carpark.exception.CarParkFullException;
import com.example.carpark.exception.NoSuitableSpacesException;
import com.example.carpark.model.ParkingSpace;
import com.example.carpark.model.Vehicle;
import com.example.carpark.model.VehicleType;
import com.example.carpark.exception.DoubleParkedException;
import com.example.carpark.exception.VehicleNotFoundException;
import com.example.carpark.util.CarParkDataGenerator;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class CarParkServiceImpl implements CarParkService {

    private CarParkDataGenerator carParkDataGenerator;

    private ParkingAuditService parkingAuditService;

    private PaymentService paymentService;

    /* This is initialised to a synchronized list for thread safety. Alternatively, we could have added the synchronized
     * keyword to the parkVehicle method, but we don't want to unnecessarily block threads unless they are accessing
     * the same ParkingSpaces in the list - i.e. it is a perfectly acceptable scenario for two cars to park in
     * different spaces simultaneously. */
    private List<ParkingSpace> parkingSpaces;

    @Value("${carPark.capacity}")
    private int capacity;

    private int numCarsParked;

    @Autowired
    public CarParkServiceImpl(CarParkDataGenerator carParkDataGenerator, ParkingAuditService parkingAuditService, PaymentService paymentService) {
        this.carParkDataGenerator = carParkDataGenerator;
        this.parkingAuditService = parkingAuditService;
        this.paymentService = paymentService;
    }

    @PostConstruct
    void initialiseCarPark() {
        log.info("Initialising car park with " + capacity + " spaces.");
        this.parkingSpaces = carParkDataGenerator.generateParkingSpaces(capacity);
        this.numCarsParked = 0;
    }

    @Override
    public ParkingSpace parkVehicle(Vehicle vehicle) throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        ParkingSpace parkingSpace;

        // Check to see if the car park is full.
        if (isCarParkFull()) {
            log.debug("parkVehicle invoked for " + vehicle.getVehicleType() + " " + vehicle.getVrm() +
                    " but the car park is full");
            throw new CarParkFullException("Car park is full, unable to park vehicle");
        }

        // Check we are not double parking.
        if (findParkedVehiclesSpace(vehicle).isPresent()) {
            log.warn("parkVehicle invoked for " + vehicle.getVehicleType() + " " + vehicle.getVrm() +
                    " but vehicle is already in the car park");
            throw new DoubleParkedException("Vehicle is already parked in this car park");
        }

        // Check to see if the car park has a suitable space (we are assuming an EV requires a charging point).
        Optional<ParkingSpace> parkingSpaceToOccupyOpt = findSuitableAvailableParkingSpace(vehicle);
        if (!parkingSpaceToOccupyOpt.isPresent()) {
            log.debug("parkVehicle invoked for " + vehicle.getVehicleType() + " " + vehicle.getVrm() +
                    " but a suitable parking space couldn't be found");
            throw new NoSuitableSpacesException("Car park does not contain a space suitable for your vehicle");
        } else {
            log.info(vehicle.getVehicleType() + " " + vehicle.getVrm() + " is being allocated a space");
            parkingSpace = parkingSpaceToOccupyOpt.get();

            // A suitable free space has been located - lets occupy it.
            occupyParkingSpace(parkingSpace, vehicle);
            numCarsParked++;
        }

        return parkingSpace;
    }

    @Override
    public double leaveCarPark(Vehicle vehicle) throws VehicleNotFoundException {
        double parkingCost;
        Optional<ParkingSpace> vehiclesParkingSpaceOpt = findParkedVehiclesSpace(vehicle);

        if (vehiclesParkingSpaceOpt.isPresent()) {
            log.info(vehicle.getVehicleType() + " " + vehicle.getVrm() + " is leaving the car park");
            ParkingSpace parkingSpace = vehiclesParkingSpaceOpt.get();

            // Vehicle has been found in the car park - calculate the parking cost...
            LocalDateTime leavingDateTime = LocalDateTime.now();
            parkingCost = paymentService.calculateParkingCost(parkingSpace.getArrival(), leavingDateTime);

            // ...and log the visit to the AuditService...
            parkingAuditService.recordParkingVisit(parkingSpace.getDesignation(), vehicle.getVrm(),
                    parkingSpace.getArrival(), leavingDateTime, parkingCost);

            log.debug(vehicle.getVehicleType() + " " + vehicle.getVrm() + " was parked for: " +
                    paymentService.calculateParkingDurationSecondsRounded(parkingSpace.getArrival(), leavingDateTime)
                    + " second(s). Parking cost (rounded to nearest hour): £" + parkingCost);

            // ...free up the space for a new vehicle to use...
            clearParkingSpace(parkingSpace);
            numCarsParked--;
        } else {
            log.warn("leaveCarPark invoked for " + vehicle.getVehicleType() + " " + vehicle.getVrm() +
                    " but vehicle not found in car park");
            throw new VehicleNotFoundException("Vehicle is not parked in this car park");
        }

        return parkingCost;
    }

    @Override
    public boolean isSuitableParkingAvailable(Vehicle vehicle) {
        Optional<ParkingSpace> freeSpaceOpt = findSuitableAvailableParkingSpace(vehicle);
        if (freeSpaceOpt.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCarParkFull() {
        return getNumCarsParked() == capacity;
    }

    @Override
    public int getNumCarsParked() {
        return numCarsParked;
    }

    @Override
    public Vehicle getRandomlyParkedVehicle() {
        Vehicle randomVehicle = null;

        // Find all occupied parking spaces and map to (occupying) Vehicle.
        List<Vehicle> parkedVehicles = parkingSpaces.stream()
                .map(ParkingSpace::getOccupyingVehicle)
                .filter(occupyingVehicle -> occupyingVehicle != null)
                .collect(Collectors.toList());

        // Shuffle the parked vehicles and return the first.
        if (!parkedVehicles.isEmpty()) {
            Collections.shuffle(parkedVehicles);
            randomVehicle = parkedVehicles.get(0);
        }

        return randomVehicle;
    }

    private Optional<ParkingSpace> findParkedVehiclesSpace(Vehicle vehicle) {
        return parkingSpaces.stream()
                .filter(parkingSpace -> parkingSpace.getOccupyingVehicle() != null
                        && parkingSpace.getOccupyingVehicle().equals(vehicle))
                .findAny();
    }

    private Optional<ParkingSpace> findSuitableAvailableParkingSpace(Vehicle vehicle) {
        Stream<ParkingSpace> parkingSpaceStream = parkingSpaces.stream()
                .filter(parkingSpace -> parkingSpace.getOccupyingVehicle() == null);

        if (vehicle.getVehicleType() == VehicleType.EV) {
            parkingSpaceStream = parkingSpaceStream.filter(ParkingSpace::isEvChargingPoint);
        } else {
            parkingSpaceStream = parkingSpaceStream.filter(parkingSpace -> !parkingSpace.isEvChargingPoint());
        }

        return parkingSpaceStream.findFirst();
    }

    private void occupyParkingSpace(ParkingSpace parkingSpace, Vehicle vehicle) {
        parkingSpace.setArrival(LocalDateTime.now());
        parkingSpace.setOccupyingVehicle(vehicle);
    }

    private void clearParkingSpace(ParkingSpace parkingSpace) {
        parkingSpace.setArrival(null);
        parkingSpace.setOccupyingVehicle(null);
    }
}