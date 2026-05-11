package com.licensing.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "license")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_activation_date")
    private LocalDate firstActivationDate;

    @Column(name = "ending_date")
    private LocalDate endingDate;

    private boolean blocked = false;

    @Column(name = "device_count", nullable = false)
    private Integer deviceCount = 1;

    private String description;
}