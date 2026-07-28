package com.travelagency.ms_tracking.dto;

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
}
