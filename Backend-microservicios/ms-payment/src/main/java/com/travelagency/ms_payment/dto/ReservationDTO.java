package com.travelagency.ms_payment.dto;

import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;
    private String userEmail;
    private Long bundleId;
    private String bundleName;
    private Integer numberOfPassengers;
    private Integer totalAmount;
    private String state;
}
