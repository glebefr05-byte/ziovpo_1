package com.licensing.repository;

import com.licensing.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findFirstByMacAddress(String macAddress);
    boolean existsByMacAddress(String macAddress);
    Optional<Device> findByMacAddressAndUserId(String macAddress, UUID userId);
}