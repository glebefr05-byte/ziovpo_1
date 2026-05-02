package com.licensing.repository;

import com.licensing.entities.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}