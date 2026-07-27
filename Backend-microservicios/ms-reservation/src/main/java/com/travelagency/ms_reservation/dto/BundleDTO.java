package com.travelagency.ms_reservation.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BundleDTO {
    private Long idBundle;
    private String nameBundle;
    private String destinationBundle;
    private LocalDate startDateBundle;
    private LocalDate endDateBundle;
    private Integer priceBundle;
    private Integer availableSlotsBundle;
    private String stateBundle;
    private LocalDate promoStartDate;
    private LocalDate promoEndDate;
    private Double promoDiscountPercent;
}
