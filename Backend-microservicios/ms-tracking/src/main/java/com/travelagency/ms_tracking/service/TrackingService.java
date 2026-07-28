package com.travelagency.ms_tracking.service;

import com.travelagency.ms_tracking.dto.BundleDTO;
import com.travelagency.ms_tracking.dto.ReservationDTO;
import com.travelagency.ms_tracking.dto.ReservationReceiptDTO;
import com.travelagency.ms_tracking.dto.TrackingDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final RestTemplate restTemplate;

    public List<ReservationDTO> getUserReservations(String userEmail) {
        String url = "http://ms-reservation/api/v1/reservations/my-reservations";

        HttpHeaders headers = new HttpHeaders();
        headers.set("userEmail", userEmail);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ReservationDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ReservationDTO>>() {}
        );

        return response.getBody();
    }

    public ReservationReceiptDTO getReceipt(Long reservationId, String userEmail, boolean isAdmin) {
        String url = "http://ms-reservation/api/v1/reservations/" + reservationId + "/receipt";

        HttpHeaders headers = new HttpHeaders();
        headers.set("userEmail", userEmail);
        headers.set("isAdmin", String.valueOf(isAdmin));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ReservationReceiptDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ReservationReceiptDTO.class
        );

        return response.getBody();
    }

    public TrackingDetailDTO getTrackingDetail(Long reservationId, String userEmail, boolean isAdmin) {
        // 1. Fetch reservation from ms-reservation
        String resUrl = "http://ms-reservation/api/v1/reservations/" + reservationId;
        ReservationDTO reservation;
        try {
            reservation = restTemplate.getForObject(resUrl, ReservationDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Reservation not found with ID: " + reservationId);
        }

        if (reservation == null) {
            throw new RuntimeException("Reservation not found with ID: " + reservationId);
        }

        // Permission check
        if (!isAdmin && !reservation.getUserEmail().equals(userEmail)) {
            throw new IllegalStateException("You can only track your own reservations.");
        }

        // 2. Fetch bundle details from ms-bundle
        String bundleUrl = "http://ms-bundle/api/v1/bundle/" + reservation.getBundleId();
        BundleDTO bundle = null;
        try {
            bundle = restTemplate.getForObject(bundleUrl, BundleDTO.class);
        } catch (Exception e) {
            // Non-critical fallback
        }

        // 3. Assemble Tracking Detail
        TrackingDetailDTO detail = new TrackingDetailDTO();
        detail.setReservationId(reservation.getId());
        detail.setStatus(reservation.getState());
        detail.setClientEmail(reservation.getUserEmail());
        detail.setReservationDate(reservation.getReservationDate());
        detail.setTotalPaid(reservation.getTotalAmount());
        detail.setNumberOfPassengers(reservation.getNumberOfPassengers());
        detail.setBundleName(reservation.getBundleName());

        if (bundle != null) {
            detail.setDestination(bundle.getDestinationBundle());
            detail.setTravelStartDate(bundle.getStartDateBundle());
            detail.setTravelEndDate(bundle.getEndDateBundle());
        }

        // Dynamic tracking message based on state
        switch (reservation.getState()) {
            case "CONFIRMED":
                detail.setTrackingMessage("Your payment has been received! Reservation confirmed. Enjoy your trip!");
                break;
            case "PENDING_PAYMENT":
                detail.setTrackingMessage("Your reservation is pending payment. Please process payment within 24 hours to hold your slots.");
                break;
            case "CANCELED":
                detail.setTrackingMessage("This reservation has been canceled.");
                break;
            default:
                detail.setTrackingMessage("Current status: " + reservation.getState());
                break;
        }

        return detail;
    }
}
