package com.travelagency.ms_reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDetailDTO {
    private String type;
    private String description;
    private Double percentage;
    private Integer amount;
}
