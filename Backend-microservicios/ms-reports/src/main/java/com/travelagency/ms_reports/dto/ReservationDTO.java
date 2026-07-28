package com.travelagency.ms_reports.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationDTO {
    private Long id;
    private String userEmail;
    private Long bundleId;
    private String bundleName;
    private Integer numberOfPassengers;
    private LocalDate reservationDate;
    private Integer totalAmount;
    private String state;
}
