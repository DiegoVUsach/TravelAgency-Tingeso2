package com.travelagency.ms_tracking.controller;

import com.travelagency.ms_tracking.dto.ReservationDTO;
import com.travelagency.ms_tracking.dto.ReservationReceiptDTO;
import com.travelagency.ms_tracking.dto.TrackingDetailDTO;
import com.travelagency.ms_tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    // Direct route /api/v1/reservations/my-reservations forwarded to ms-tracking by Gateway
    @GetMapping("/api/v1/reservations/my-reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @RequestHeader("userEmail") String userEmail) {
        List<ReservationDTO> reservations = trackingService.getUserReservations(userEmail);
        return ResponseEntity.ok(reservations);
    }

    // Direct route /api/v1/reservations/{id}/receipt forwarded to ms-tracking by Gateway
    @GetMapping("/api/v1/reservations/{id}/receipt")
    public ResponseEntity<ReservationReceiptDTO> getReceipt(
            @PathVariable Long id,
            @RequestHeader("userEmail") String userEmail,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {

        ReservationReceiptDTO receipt = trackingService.getReceipt(id, userEmail, isAdmin);
        return ResponseEntity.ok(receipt);
    }

    // Direct tracking route /api/v1/tracking/{id}
    @GetMapping("/api/v1/tracking/{id}")
    public ResponseEntity<TrackingDetailDTO> getTrackingDetail(
            @PathVariable Long id,
            @RequestHeader("userEmail") String userEmail,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {

        TrackingDetailDTO detail = trackingService.getTrackingDetail(id, userEmail, isAdmin);
        return ResponseEntity.ok(detail);
    }
}
