package com.example.carpark.service;

import com.example.carpark.model.ParkingAudit;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ParkingAuditServiceTest {

    private ParkingAuditService parkingAuditService = new ParkingAuditServiceImpl();

    @Test
    void canRecordAndRetrieveSingleCarParkVisit() {
        ParkingAudit parkingAuditToRecord = Instancio.create(ParkingAudit.class);
        parkingAuditService.recordParkingVisit(parkingAuditToRecord);

        List<ParkingAudit> parkingAudits = parkingAuditService.getAllParkingAudits();
        assertThat(parkingAudits.size(), equalTo(1));
        assertThat(parkingAudits.get(0), equalTo(parkingAuditToRecord));
    }

    @Test
    void canRecordAndRetrieveMultipleCarParkVisit() {
        ParkingAudit parkingAuditToRecordOne = Instancio.create(ParkingAudit.class);
        ParkingAudit parkingAuditToRecordTwo = Instancio.create(ParkingAudit.class);
        parkingAuditService.recordParkingVisit(parkingAuditToRecordOne);
        parkingAuditService.recordParkingVisit(parkingAuditToRecordTwo);

        List<ParkingAudit> parkingAudits = parkingAuditService.getAllParkingAudits();
        assertThat(parkingAudits.size(), equalTo(2));
        assertThat(parkingAudits.get(0), equalTo(parkingAuditToRecordOne));
        assertThat(parkingAudits.get(1), equalTo(parkingAuditToRecordTwo));
    }

    @Test
    void canRetrieveParkingAuditsForGivenDateRange() {
        // 01/04/2022 9AM - 10:30AM
        ParkingAudit parkingAuditToRecordOne = new ParkingAudit(1, "aa51 aaa",
                LocalDateTime.of(2022, Month.APRIL, 1, 9, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 1, 10, 30, 40), 4.00);
        // 03/04/2022 10AM - 10:30AM
        ParkingAudit parkingAuditToRecordTwo = new ParkingAudit(3, "bb51 bbb",
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 30, 0), 2.00);

        parkingAuditService.recordParkingVisit(parkingAuditToRecordOne);
        parkingAuditService.recordParkingVisit(parkingAuditToRecordTwo);

        // Audit search bounds: 01/04/2022 midnight - 02/04/2022 midnight.
        LocalDateTime auditStart = LocalDateTime.of(2022, Month.APRIL, 1, 0, 0, 0);
        LocalDateTime auditEnd = LocalDateTime.of(2022, Month.APRIL, 2, 0, 0, 0);
        List<ParkingAudit> parkingAudits = parkingAuditService.getParkingAuditsForDateRange(auditStart, auditEnd);

        assertThat(parkingAudits.size(), equalTo(1));
        assertThat(parkingAudits.get(0), equalTo(parkingAuditToRecordOne));
    }

    @Test
    void canCalculateTotalFundsCollectedForDateRange() {
        // 01/04/2022 9AM - 10:30AM. 4.00 payment collected.
        ParkingAudit parkingAuditToRecordOne = new ParkingAudit(1, "aa51 aaa",
                LocalDateTime.of(2022, Month.APRIL, 1, 9, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 1, 10, 30, 40), 4.00);
        // 03/04/2022 10AM - 10:30AM. 2.00 payment collected.
        ParkingAudit parkingAuditToRecordTwo = new ParkingAudit(3, "bb51 bbb",
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 30, 0), 2.00);

        parkingAuditService.recordParkingVisit(parkingAuditToRecordOne);
        parkingAuditService.recordParkingVisit(parkingAuditToRecordTwo);

        // Audit search bounds: 01/04/2022 midnight - 04/04/2022 midnight.
        LocalDateTime auditStart = LocalDateTime.of(2022, Month.APRIL, 1, 0, 0, 0);
        LocalDateTime auditEnd = LocalDateTime.of(2022, Month.APRIL, 4, 0, 0, 0);

        assertThat(parkingAuditService.getTotalFundsCollectedForDateRange(auditStart, auditEnd), equalTo(6.00));
    }

    @Test
    void getDistinctNumberOfVehiclesVisitedForDateRange() {
        // 01/04/2022 9AM - 10:30AM. Total vehicles visited: 1 vehicle.
        ParkingAudit parkingAuditToRecordOne = new ParkingAudit(1, "aa51 aaa",
                LocalDateTime.of(2022, Month.APRIL, 1, 9, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 1, 10, 30, 40), 4.00);
        // 03/04/2022 10AM - 10:30AM. Total vehicles visited: 2 vehicle.
        ParkingAudit parkingAuditToRecordTwo = new ParkingAudit(3, "bb51 bbb",
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 0, 0),
                LocalDateTime.of(2022, Month.APRIL, 3, 10, 30, 0), 2.00);

        parkingAuditService.recordParkingVisit(parkingAuditToRecordOne);
        parkingAuditService.recordParkingVisit(parkingAuditToRecordTwo);

        // Audit search bounds: 01/04/2022 midnight - 04/04/2022 midnight.
        LocalDateTime auditStart = LocalDateTime.of(2022, Month.APRIL, 1, 0, 0, 0);
        LocalDateTime auditEnd = LocalDateTime.of(2022, Month.APRIL, 4, 0, 0, 0);

        assertThat(parkingAuditService.getDistinctNumberOfVehiclesVisitedForDateRange(auditStart, auditEnd), equalTo(2));
    }

    // TODO: (If time allowed) Add edge cases tests for all date range functions.
    // TODO: (If time allowed) Check distinct clause is working for getDistinctNumberOfVehiclesVisitedForDateRange.
}
