package com.example.carpark.service;

import com.example.carpark.model.ParkingAudit;

import java.time.LocalDateTime;
import java.util.List;

public interface ParkingAuditService {

    /**
     * Records a parking visit (vehicle successfully parked then subsequently left) in the car park audit trail.
     * @param parkingSpaceDesignation the designation of the parking space used during the visit.
     * @param vehicleVrm the vrm of the parked vehicle during the visit.
     * @param arrived the date time of vehicle arrival in the car parking space.
     * @param left the date time the vehicle left the car parking space.
     * @param parkingCost the parking cost accrued during the car park visit.
     */
    void recordParkingVisit(int parkingSpaceDesignation, String vehicleVrm, LocalDateTime arrived,
                                   LocalDateTime left, double parkingCost);

    /**
     * Records a parking visit (vehicle successfully parked then subsequently left) in the car park audit trail.
     * @param parkingAudit the parking audit object to be added to the car park audit trail.
     */
    void recordParkingVisit(ParkingAudit parkingAudit);

    /**
     * Returns the entire car park audit trail.
     * @return a list of all parking audits contained in the car park audit trail.
     */
    List<ParkingAudit> getAllParkingAudits();

    /**
     * Returns all parking audits from the car park audit trail that fall between the start and end date times (inclusive).
     * @param start the date time to begin capturing car park audits from (inclusive).
     * @param end the date time to end capturing car park audits from (inclusive).
     * @return a list of all parking audits where the vehicle entered then left the car park in the provided date range (inclusive).
     */
    List<ParkingAudit> getParkingAuditsForDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Returns the total parking charges collected from vehicles that arrived and subsequently left between the supplied
     * start and end date times (inclusive).
     * @param start the date time to begin capturing collected funds from (inclusive).
     * @param end the date time to end capturing collected funds from (inclusive).
     * @return the total amount (in pounds) collected from parked vehicles during the supplied
     */
    double getTotalFundsCollectedForDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Returns the total number of vehicles that arrived and subsequently left between the supplied start and end date
     * times (inclusive).
     * @param start the date time to begin capturing the number of parked vehicles from (inclusive).
     * @param end the date time to end capturing the number of parked vehicles from (inclusive).
     * @return the total number of vehicles that entered then left the car park in the provided date range (inclusive).
     */
    int getDistinctNumberOfVehiclesVisitedForDateRange(LocalDateTime start, LocalDateTime end);
}
