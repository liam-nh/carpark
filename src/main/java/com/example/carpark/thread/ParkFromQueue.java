package com.example.carpark.thread;

import com.example.carpark.exception.CarParkFullException;
import com.example.carpark.exception.DoubleParkedException;
import com.example.carpark.exception.NoSuitableSpacesException;
import com.example.carpark.model.Vehicle;
import com.example.carpark.service.CarParkService;
import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class ParkFromQueue implements Runnable {

    private CarParkService carParkService;

    private BlockingQueue<Vehicle> parkingQueue;

    @Override
    public void run() {
        // Delay execution of this thread by 2 seconds to allow the queue to be semi-populated with vehicles.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (!parkingQueue.isEmpty()) {
            boolean parkingSuccessful = true;
            Vehicle firstWaitingToPark = parkingQueue.peek();

            // Attempt to park the first vehicle waiting in the queue.
            try {
                carParkService.parkVehicle(firstWaitingToPark);
            } catch (DoubleParkedException e) {
                throw new RuntimeException(e);
            } catch (CarParkFullException | NoSuitableSpacesException e) {
                // Vehicle will remain in the queue - try parking it again on the next execution.
                parkingSuccessful = false;
            }

            // If vehicle has been successfully parked, we can remove it from the queue.
            if (parkingSuccessful) {
                parkingQueue.poll();
            }

            // Sleep for 1 second before attempting to park the vehicle at the head of the queue.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
