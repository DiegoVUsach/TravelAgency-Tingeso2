package com.travelagency.ms_payment.service;

import com.travelagency.ms_payment.dto.ReservationDTO;
import com.travelagency.ms_payment.entity.PaymentEntity;
import com.travelagency.ms_payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public String processPayment(Long reservationId, Integer amount, String method, String callerEmail) {

        // 1. Fetch reservation from ms-reservation
        String reservationUrl = "http://ms-reservation/api/v1/reservations/" + reservationId;
        ReservationDTO reservation;
        try {
            reservation = restTemplate.getForObject(reservationUrl, ReservationDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Reservation not found or ms-reservation is down.");
        }

        if (reservation == null) {
            throw new RuntimeException("Reservation not found.");
        }

        // 2. Validate Reservation State
        if ("CANCELED".equals(reservation.getState())) {
            throw new IllegalStateException("Cannot pay for a canceled reservation.");
        }

        if ("CONFIRMED".equals(reservation.getState())) {
            throw new IllegalStateException("This reservation is already paid.");
        }

        // 3. Verify that the caller is the owner of the reservation
        if (!reservation.getUserEmail().equals(callerEmail)) {
            throw new IllegalStateException("You cannot pay for a reservation that does not belong to you.");
        }

        // 4. Validate Amount
        if (!reservation.getTotalAmount().equals(amount)) {
            throw new IllegalArgumentException("The amount must match the total reservation price: " + reservation.getTotalAmount());
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }

        // 5. Process Payment and Save
        PaymentEntity payment = new PaymentEntity();
        payment.setReservationId(reservationId);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(method); // placeholder for actual payment method
        
        paymentRepository.save(payment);

        // 6. Update reservation state to CONFIRMED in ms-reservation
        String updateStateUrl = "http://ms-reservation/api/v1/reservations/" + reservationId + "/state?newState=CONFIRMED";
        try {
            // We use PATCH which is mapped to @PatchMapping in ms-reservation
            restTemplate.exchange(updateStateUrl, HttpMethod.PATCH, null, Void.class);
        } catch (Exception e) {
            // In a real system, you'd use the Saga pattern or outbox pattern to guarantee consistency here.
            // For this project, a simple HTTP call is sufficient.
            throw new RuntimeException("Payment succeeded but failed to confirm the reservation status in ms-reservation.", e);
        }

        return "Payment processed successfully. Reservation " + reservationId + " is now CONFIRMED.";
    }
}
