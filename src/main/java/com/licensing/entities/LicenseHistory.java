package com.licensing.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "license_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String status;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    private String description;
}