package com.example.carpark.thread;

import com.example.carpark.model.Vehicle;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class AddToParkingQueue implements Runnable {

    private static final Random RANDOM = new Random();

    private List<Vehicle> allVehicles;

    private BlockingQueue<Vehicle> parkingQueue;

    @Override
    public void run() {
        while (!allVehicles.isEmpty()) {
            // Get a random vehicle from the list of all vehicles.
            Vehicle randomVehicle = allVehicles.get(RANDOM.nextInt(allVehicles.size()));

            // Remove vehicle from the list of all vehicles and add to the parking queue.
            // Refactoring may be required later if we decide the queue is of finite size.
            if (allVehicles.remove(randomVehicle)) {
                parkingQueue.offer(randomVehicle);

                // Sleep for 500 millis before attempting to add next vehicle to the parking queue.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
