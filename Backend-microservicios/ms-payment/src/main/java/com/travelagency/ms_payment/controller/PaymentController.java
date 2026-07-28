package com.travelagency.ms_payment.controller;

import com.travelagency.ms_payment.dto.PaymentRequestDTO;
import com.travelagency.ms_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Security is simulated via header for now until API Gateway Keycloak is setup
    @PostMapping
    public ResponseEntity<String> processPayment(
            @RequestHeader("userEmail") String callerEmail,
            @RequestBody PaymentRequestDTO request) {

        String result = paymentService.processPayment(
                request.getReservationId(),
                request.getAmount(),
                request.getPaymentMethod(),
                callerEmail);

        return ResponseEntity.ok(result);
    }
}
