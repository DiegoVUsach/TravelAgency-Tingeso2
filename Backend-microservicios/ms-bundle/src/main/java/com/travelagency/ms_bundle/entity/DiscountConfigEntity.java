package com.travelagency.ms_bundle.entity;

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

    // Descriptive name (e.g. "VOLUME_DISCOUNT", "FREQUENT_CLIENT")
    @Column(unique = true, nullable = false)
    private String configKey;

    // Numeric value (e.g. 0.10 for 10%)
    @Column(nullable = false)
    private Double configValue;

    // Activation threshold (e.g. 4 passengers or 3 previous reservations)
    private Integer threshold;

    private String description;
}
