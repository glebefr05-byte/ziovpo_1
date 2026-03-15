package com.licensing.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_license")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "activation_date", nullable = false)
    private LocalDateTime activationDate;
}