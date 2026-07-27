package com.travelagency.ms_reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discount_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String configKey;

    @Column(nullable = false)
    private Double configValue;

    private Integer threshold;

    private String description;
}
