package com.example.rbpo_aeroport.entities;

import com.example.rbpo_aeroport.models.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 1024)
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser user;

    @Column(nullable = false)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant revokedAt;
}
