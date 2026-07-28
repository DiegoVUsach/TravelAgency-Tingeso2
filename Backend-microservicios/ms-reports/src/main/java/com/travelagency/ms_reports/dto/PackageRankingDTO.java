package com.travelagency.ms_reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageRankingDTO {
    private Long bundleId;
    private String bundleName;
    private Long totalReservations;
    private Long totalPassengers;
    private Long totalRevenue;
}
