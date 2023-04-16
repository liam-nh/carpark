package com.example.carpark.service;

import com.example.carpark.exception.CarParkFullException;
import com.example.carpark.exception.NoSuitableSpacesException;
import com.example.carpark.model.ParkingSpace;
import com.example.carpark.model.Vehicle;
import com.example.carpark.model.VehicleType;
import com.example.carpark.exception.DoubleParkedException;
import com.example.carpark.exception.VehicleNotFoundException;
import com.example.carpark.util.CarParkDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CarParkServiceTest {

    @Mock
    private CarParkDataGenerator carParkDataGenerator;

    @Mock
    private PaymentServiceImpl paymentService;

    @Mock
    private ParkingAuditService parkingAuditService;

    @InjectMocks
    private CarParkServiceImpl carParkService;

    private static final int CAR_PARK_CAPACITY = 3;

    private static final double HOURLY_RATE = 2.0;

    @BeforeEach
    void init() {
        // Empty Car Park with three spaces - one EV and two "normal".
        List<ParkingSpace> emptyCarPark = new ArrayList<>();
        emptyCarPark.add(new ParkingSpace(1, false));
        emptyCarPark.add(new ParkingSpace(2, true));
        emptyCarPark.add(new ParkingSpace(3, false));

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(carParkService, "capacity", CAR_PARK_CAPACITY);

        when(carParkDataGenerator.generateParkingSpaces(CAR_PARK_CAPACITY)).thenReturn(emptyCarPark);
        when(paymentService.calculateParkingCost(any(), any())).thenReturn(HOURLY_RATE);

        // Refresh car park back to three empty spaces before each test.
        carParkService.initialiseCarPark();
    }

    @Test
    void canParkOneVehicleInEmptyCarPark() throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertThat(carParkService.getNumCarsParked(), equalTo(1));
        assertFalse(carParkService.isCarParkFull());
    }

    @Test
    void cantDoublePark() throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertThrows(DoubleParkedException.class, () -> carParkService.parkVehicle(myVehicle));
    }

    @Test
    void canBringCarParkUpToFullCapacity() throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myEv = new Vehicle("ev51 eev", VehicleType.EV);
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);
        Vehicle myVehicleTwo = new Vehicle("bb51 bbb", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myEv));
        assertNotNull(carParkService.parkVehicle(myVehicle));
        // Does the last remaining space suit our vehicle type?
        assertTrue(carParkService.isSuitableParkingAvailable(myVehicleTwo));
        assertNotNull(carParkService.parkVehicle(myVehicleTwo));
        assertThat(carParkService.getNumCarsParked(), equalTo(3));
        assertTrue(carParkService.isCarParkFull());
    }

    @Test
    void cantParkVehicleWhenCarParkIsFull() throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myEv = new Vehicle("ev51 eev", VehicleType.EV);
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);
        Vehicle myVehicleTwo = new Vehicle("bb51 bbb", VehicleType.CAR);
        Vehicle myVehicleThree = new Vehicle("cc51 ccc", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myEv));
        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertNotNull(carParkService.parkVehicle(myVehicleTwo));
        assertThat(carParkService.getNumCarsParked(), equalTo(3));

        // Try to park a fourth vehicle.
        assertThrows(CarParkFullException.class, () -> carParkService.parkVehicle(myVehicleThree));
        // Verify car park still only contains three vehicles.
        assertThat(carParkService.getNumCarsParked(), equalTo(3));
    }

    @Test
    void cantParkVehicleIfFreeSpaceIsWrongType() throws DoubleParkedException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);
        Vehicle myVehicleTwo = new Vehicle("bb51 bbb", VehicleType.CAR);
        Vehicle myVehicleThree = new Vehicle("cc51 ccc", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertNotNull(carParkService.parkVehicle(myVehicleTwo));
        assertThat(carParkService.getNumCarsParked(), equalTo(2));

        // Try to park a third vehicle - the last remaining space is for EV, so not suitable.
        assertFalse(carParkService.isSuitableParkingAvailable(myVehicleThree));
        assertThrows(NoSuitableSpacesException.class, () -> carParkService.parkVehicle(myVehicleThree));
        // Verify car park still only contains two vehicles.
        assertThat(carParkService.getNumCarsParked(), equalTo(2));
    }

    @Test
    void leavingCarParkFreesUpASpace() throws DoubleParkedException, VehicleNotFoundException, CarParkFullException, NoSuitableSpacesException {
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);

        // Park one vehicle.
        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertThat(carParkService.getNumCarsParked(), equalTo(1));

        // Vehicle leaves - ensure space is recovered.
        carParkService.leaveCarPark(myVehicle);
        assertThat(carParkService.getNumCarsParked(), equalTo(0));
    }

    @Test
    void cantLeaveIfNeverParked() {
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);
        // Try to leave an empty car park.
        assertThrows(VehicleNotFoundException.class, () -> carParkService.leaveCarPark(myVehicle));
    }

    @Test
    void canReturnARandomlyParkedVehicle() throws CarParkFullException, DoubleParkedException, NoSuitableSpacesException {
        Vehicle myEv = new Vehicle("ev51 eev", VehicleType.EV);
        Vehicle myVehicle = new Vehicle("aa51 aaa", VehicleType.CAR);
        Vehicle myVehicleTwo = new Vehicle("bb51 bbb", VehicleType.CAR);

        assertNotNull(carParkService.parkVehicle(myEv));
        assertNotNull(carParkService.parkVehicle(myVehicle));
        assertNotNull(carParkService.parkVehicle(myVehicleTwo));

        List<Vehicle> parkedVehicles = List.of(myEv, myVehicle, myVehicleTwo);
        Vehicle randomlyParkedVehicle = carParkService.getRandomlyParkedVehicle();

        assertNotNull(randomlyParkedVehicle);
        assertTrue(parkedVehicles.contains(randomlyParkedVehicle));
    }
}
