package com.example.rbpo_aeroport.repositories;

import com.example.rbpo_aeroport.entities.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, UUID> {
    Optional<ApplicationUser> findByEmail(String email);
}
