package com.travelagency.ms_tracking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TrackingDetailDTO {
    private Long reservationId;
    private String status;
    private String clientEmail;
    private LocalDate reservationDate;
    private Integer totalPaid;
    private Integer numberOfPassengers;
    private String bundleName;
    private String destination;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private String trackingMessage;
}
