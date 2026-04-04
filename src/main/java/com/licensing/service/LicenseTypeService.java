package com.licensing.service;

import com.licensing.entities.LicenseType;
import com.licensing.repository.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseTypeService {
    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseType getTypeOrFail(UUID typeId) throws Exception {
        return licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new Exception("License type not found: " + typeId));
    }
}