package com.example.carpark.service;

import com.example.carpark.model.ParkingAudit;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingAuditServiceImpl implements ParkingAuditService {
    private List<ParkingAudit> carParkAudits;

    public ParkingAuditServiceImpl() {
        carParkAudits = new ArrayList<>();
    }

    @Override
    public void recordParkingVisit(int parkingSpaceDesignation, String vehicleVrm, LocalDateTime arrived,
                                   LocalDateTime left, double parkingCost) {
        carParkAudits.add(new ParkingAudit(parkingSpaceDesignation, vehicleVrm, arrived, left, parkingCost));
    }

    @Override
    public void recordParkingVisit(ParkingAudit parkingAudit) {
        carParkAudits.add(parkingAudit);
    }

    @Override
    public List<ParkingAudit> getAllParkingAudits() {
        return carParkAudits;
    }


    @Override
    public List<ParkingAudit> getParkingAuditsForDateRange(LocalDateTime start, LocalDateTime end) {
        return carParkAudits.stream()
                .filter(parkingAudit ->
                        (start.isEqual(parkingAudit.getArrived()) || start.isBefore(parkingAudit.getArrived())) &&
                        (end.isEqual(parkingAudit.getLeft()) || end.isAfter(parkingAudit.getLeft())))
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalFundsCollectedForDateRange(LocalDateTime start, LocalDateTime end) {
        return carParkAudits.stream()
                .filter(parkingAudit ->
                        (start.isEqual(parkingAudit.getArrived()) || start.isBefore(parkingAudit.getArrived())) &&
                        (end.isEqual(parkingAudit.getLeft()) || end.isAfter(parkingAudit.getLeft())))
                .mapToDouble(ParkingAudit::getParkingCharge)
                .sum();
    }

    @Override
    public int getDistinctNumberOfVehiclesVisitedForDateRange(LocalDateTime start, LocalDateTime end) {
        return (int) carParkAudits.stream()
                .filter(parkingAudit ->
                        (start.isEqual(parkingAudit.getArrived()) || start.isBefore(parkingAudit.getArrived())) &&
                        (end.isEqual(parkingAudit.getLeft()) || end.isAfter(parkingAudit.getLeft())))
                .map(ParkingAudit::getVehicleVrm)
                .distinct()
                .count();
    }
}
