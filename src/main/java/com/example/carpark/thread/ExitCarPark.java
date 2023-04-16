package com.example.carpark.thread;

import com.example.carpark.exception.VehicleNotFoundException;
import com.example.carpark.model.Vehicle;
import com.example.carpark.service.CarParkService;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ExitCarPark implements Runnable {

    private CarParkService carParkService;

    private List<Vehicle> allVehicles;

    @Override
    public void run() {
        // Delay execution of this thread by 10 seconds to allow the car park to fill up.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (carParkService.getNumCarsParked() > 0) {
            Vehicle randomParkedVehicle = carParkService.getRandomlyParkedVehicle();
            if (randomParkedVehicle != null) {
                // Have a random parked vehicle leave the car park.
                try {
                    carParkService.leaveCarPark(randomParkedVehicle);
                } catch (VehicleNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // Add this vehicle back into the list of all vehicles (to be re-parked).
                allVehicles.add(randomParkedVehicle);

                // Sleep for 5 seconds to simulate vehicles staying in the car park for some time.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
