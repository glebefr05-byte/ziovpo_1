package com.licensing.entities;

import com.licensing.model.enums.ApplicationUserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private ApplicationUserRole role;

    @Column(name = "is_account_expired")
    private boolean isAccountExpired = false;

    @Column(name = "is_account_locked")
    private boolean isAccountLocked = false;

    @Column(name = "is_credentials_expired")
    private boolean isCredentialsExpired = false;

    @Column(name = "is_disabled")
    private boolean isDisabled = false;
}