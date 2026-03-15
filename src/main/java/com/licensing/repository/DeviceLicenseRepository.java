package com.licensing.repository;

import com.licensing.entities.DeviceLicense;
import com.licensing.entities.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    @Query("SELECT COUNT(dl) FROM DeviceLicense dl WHERE dl.license.id = :licenseId")
    Integer countByLicenseId(@Param("licenseId") UUID licenseId);

    boolean existsByLicenseAndDeviceMacAddress(License license, String macAddress);
}