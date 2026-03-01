package com.example.rbpo_aeroport.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.example.rbpo_aeroport.models.enums.ApplicationUserRole;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUser {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private ApplicationUserRole role;

    private boolean isAccountExpired;

    private boolean isAccountLocked;

    private boolean isCredentialsExpired;

    private boolean isDisabled;
}