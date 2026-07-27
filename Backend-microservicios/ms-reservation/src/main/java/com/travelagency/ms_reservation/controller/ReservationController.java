package com.travelagency.ms_reservation.controller;

import com.travelagency.ms_reservation.dto.PackageRankingDTO;
import com.travelagency.ms_reservation.dto.ReservationReceiptDTO;
import com.travelagency.ms_reservation.dto.ReservationRequestDTO;
import com.travelagency.ms_reservation.dto.ReservationResponseDTO;
import com.travelagency.ms_reservation.entity.ReservationEntity;
import com.travelagency.ms_reservation.entity.ReservationState;
import com.travelagency.ms_reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // Security is removed for now, we simulate the email with a Header or assume it's passed from ms-tracking/gateway later.
    // For now, we accept 'userEmail' as a header to simulate authentication.
    
    @PostMapping("/cart")
    public ResponseEntity<ReservationResponseDTO> createMultipleReservations(
            @RequestHeader("userEmail") String userEmail,
            @RequestBody ReservationRequestDTO request) {

        ReservationResponseDTO response = reservationService.processCartReservations(request, userEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/quote")
    public ResponseEntity<ReservationResponseDTO> quoteReservation(
            @RequestHeader("userEmail") String userEmail,
            @RequestBody ReservationRequestDTO request) {

        ReservationResponseDTO quote = reservationService.calculateQuote(request, userEmail);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationEntity>> getMyReservations(
            @RequestHeader("userEmail") String userEmail) {
        
        List<ReservationEntity> reservations = reservationService.getUserReservations(userEmail);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        List<ReservationEntity> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<ReservationEntity> updateReservationState(
            @PathVariable Long id,
            @RequestParam ReservationState newState) {

        ReservationEntity updatedReservation = reservationService.updateReservationState(id, newState);
        return ResponseEntity.ok(updatedReservation);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReservationReceiptDTO> getReservationReceipt(
            @PathVariable Long id,
            @RequestHeader("userEmail") String callerEmail,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {
        
        ReservationReceiptDTO receipt = reservationService.generateReceipt(id, callerEmail, isAdmin);
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<List<ReservationEntity>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ReservationEntity> sales = reservationService.getSalesByPeriod(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/reports/ranking")
    public ResponseEntity<List<PackageRankingDTO>> getPackageRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PackageRankingDTO> ranking = reservationService.getPackageRanking(startDate, endDate);
        return ResponseEntity.ok(ranking);
    }

    // ========== Internal Endpoints for ms-bundle ==========

    @GetMapping("/internal/count-by-bundle/{bundleId}")
    public ResponseEntity<Long> countByBundleId(@PathVariable Long bundleId) {
        long count = reservationService.countByBundleId(bundleId);
        return ResponseEntity.ok(count);
    }
}
