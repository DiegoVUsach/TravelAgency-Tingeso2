package com.travelagency.ms_reservation.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReservationRequestDTO {
    private List<CartItemDTO> items;
}
