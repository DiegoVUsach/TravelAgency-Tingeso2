package com.travelagency.ms_reservation.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationReceiptDTO {
    private String receiptCode;
    private LocalDate issueDate;
    private String clientEmail;
    private String bundleName;
    private String destination; // we will retrieve this from ms-bundle if needed or just leave null if it's not critical
    private int numberOfPassengers;
    private int totalPaid;
    private String status;
}
