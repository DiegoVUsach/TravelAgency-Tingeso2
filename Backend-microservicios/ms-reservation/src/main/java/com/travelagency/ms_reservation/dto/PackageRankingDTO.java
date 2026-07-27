package com.travelagency.ms_reservation.dto;

public interface PackageRankingDTO {
    Long getBundleId();
    String getBundleName();
    Long getTotalReservations();
    Long getTotalPassengers();
    Long getTotalRevenue();
}
