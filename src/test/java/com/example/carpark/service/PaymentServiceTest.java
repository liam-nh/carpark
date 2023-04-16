package com.example.carpark.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentServiceTest {

    private static final double COST_PER_HOUR = 2.00;

    private PaymentService paymentService = new PaymentServiceImpl();

    @BeforeAll
    void init() {
        ReflectionTestUtils.setField(paymentService, "costPerHour", COST_PER_HOUR);
    }

    @Test
    void lessThanOneHourShouldBeHourlyRate() {
        LocalDateTime arrived = LocalDateTime.now();
        LocalDateTime left = arrived.plusMinutes(20);

        double parkingCost = paymentService.calculateParkingCost(arrived, left);
        assertThat(parkingCost, equalTo(COST_PER_HOUR));
    }

    @Test
    void oneHourShouldBeHourlyRate() {
        LocalDateTime arrived = LocalDateTime.now();
        LocalDateTime left = arrived.plusMinutes(60);

        double parkingCost = paymentService.calculateParkingCost(arrived, left);
        assertThat(parkingCost, equalTo(COST_PER_HOUR));
    }

    @Test
    void oneHourOneMinShouldBeDoubleHourlyRate() {
        LocalDateTime arrived = LocalDateTime.now();
        LocalDateTime left = arrived.plusMinutes(61);

        double parkingCost = paymentService.calculateParkingCost(arrived, left);
        assertThat(parkingCost, equalTo(COST_PER_HOUR * 2));
    }

    @Test
    void twoHoursShouldBeDoubleHourlyRate() {
        LocalDateTime arrived = LocalDateTime.now();
        LocalDateTime left = arrived.plusMinutes(120);

        double parkingCost = paymentService.calculateParkingCost(arrived, left);
        assertThat(parkingCost, equalTo(COST_PER_HOUR * 2));
    }

    @Test
    void twoHoursOneMinShouldBeTripleHourlyRate() {
        LocalDateTime arrived = LocalDateTime.now();
        LocalDateTime left = arrived.plusMinutes(121);

        double parkingCost = paymentService.calculateParkingCost(arrived, left);
        assertThat(parkingCost, equalTo(COST_PER_HOUR * 3));
    }


    /* TODO: (If time allowed) Add more test cases where parking duration <1 minute and <1 second (not real world
     * scenarios but have been witnessed during testing with current Thread sleep values. */
}