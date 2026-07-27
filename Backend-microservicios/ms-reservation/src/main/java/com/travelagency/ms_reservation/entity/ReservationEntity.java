package com.travelagency.ms_reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "Reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Decoupled from ms-users: just store the email
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    // Decoupled from ms-bundle: store ID and Name for faster retrieval in reports/receipts
    @Column(name = "bundle_id", nullable = false)
    private Long bundleId;

    @Column(name = "bundle_name", nullable = false)
    private String bundleName;

    @Column(nullable = false)
    private Integer numberOfPassengers;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private Integer totalAmount; // final amount after discounts

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationState state;
}
