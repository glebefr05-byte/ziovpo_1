package com.licensing.repository;

import com.licensing.entities.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findFirstByCode(String code);

    @Query("SELECT l FROM License l " +
            "JOIN DeviceLicense dl ON dl.license.id = l.id " +
            "JOIN Device d ON d.id = dl.device.id " +
            "WHERE d.macAddress = :macAddress " +
            "AND l.user.id = :userId " +
            "AND l.product.id = :productId " +
            "AND l.blocked = false " +
            "AND l.endingDate >= :currentDate")
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("macAddress") String macAddress,
            @Param("userId") UUID userId,
            @Param("productId") UUID productId,
            @Param("currentDate") LocalDate currentDate);
}