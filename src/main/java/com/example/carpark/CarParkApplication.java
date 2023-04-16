package com.example.carpark;

import com.example.carpark.model.Vehicle;
import com.example.carpark.service.CarParkService;
import com.example.carpark.service.CarParkServiceImpl;
import com.example.carpark.thread.AddToParkingQueue;
import com.example.carpark.thread.ExitCarPark;
import com.example.carpark.thread.ParkFromQueue;
import com.example.carpark.util.CarParkDataGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class CarParkApplication {

	private static CarParkService carParkService;

	private static CarParkDataGenerator carParkDataGenerator;

	private static int numVehiclesToModel;

	// Initialised to a synchronized list for thread safety (we have multiple threads retrieving/removing/adding vehicles).
	private static List<Vehicle> allVehicles;

	/* Initialised to a LinkedBlockingQueue for a thread safe parking queue (we may want to spawn multiple
	 * "ParkFromQueue" threads in future to clear a backlog). */
	private static BlockingQueue<Vehicle> parkingQueue;

	public static void main(String[] args) {
		// Wire necessary beans/properties in this static context.
		ApplicationContext applicationContext = SpringApplication.run(CarParkApplication.class, args);
		carParkService = applicationContext.getBean(CarParkServiceImpl.class);
		carParkDataGenerator = applicationContext.getBean(CarParkDataGenerator.class);
		numVehiclesToModel = Integer.parseInt(applicationContext.getEnvironment().getProperty("run.numVehiclesToModel"));
		parkingQueue = new LinkedBlockingQueue<>();

		// Instantiate 120 random vehicles to populate the 100 space car park.
		allVehicles = carParkDataGenerator.generateRandomVehicles(numVehiclesToModel);

		// First thread populates the queue (waiting to park) from the list of random vehicles.
		AddToParkingQueue addToParkingQueue = new AddToParkingQueue(allVehicles, parkingQueue);
		Thread addToParkingQueueThread = new Thread(addToParkingQueue);

		// Second thread attempts to park vehicles waiting in the queue in the car park.
		ParkFromQueue parkFromQueue = new ParkFromQueue(carParkService, parkingQueue);
		Thread parkFromQueueThread = new Thread(parkFromQueue);

		// Third thread will select parked vehicles (at random), have them leave the car park,
		// then return them back to the list of random vehicles.
		ExitCarPark exitCarPark = new ExitCarPark(carParkService, allVehicles);
		Thread exitCarParkThread = new Thread(exitCarPark);

		addToParkingQueueThread.start();
		parkFromQueueThread.start();
		exitCarParkThread.start();
	}
}