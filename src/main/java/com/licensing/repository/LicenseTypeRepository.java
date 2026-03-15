package com.licensing.repository;

import com.licensing.entities.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
    Optional<LicenseType> findByName(String name);
    boolean existsByName(String name);
}