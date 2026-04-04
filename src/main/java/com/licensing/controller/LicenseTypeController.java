package com.licensing.controller;

import com.licensing.entities.LicenseType;
import com.licensing.model.LicenseTypeDto;
import com.licensing.repository.LicenseTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/license-types")
@RequiredArgsConstructor
public class LicenseTypeController {

    private final LicenseTypeRepository licenseTypeRepository;

    private LicenseTypeDto convertToDto(LicenseType licenseType) {
        LicenseTypeDto dto = new LicenseTypeDto();
        dto.setId(licenseType.getId());
        dto.setName(licenseType.getName());
        dto.setDefaultDurationInDays(licenseType.getDefaultDurationInDays());
        dto.setDescription(licenseType.getDescription());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<LicenseTypeDto>> getAllLicenseTypes() {
        List<LicenseTypeDto> licenseTypes = licenseTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(licenseTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicenseTypeDto> getLicenseTypeById(@PathVariable UUID id) {
        LicenseType licenseType = licenseTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License type not found with id: " + id));
        return ResponseEntity.ok(convertToDto(licenseType));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<LicenseTypeDto> getLicenseTypeByName(@PathVariable String name) {
        LicenseType licenseType = licenseTypeRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License type not found with name: " + name));
        return ResponseEntity.ok(convertToDto(licenseType));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseTypeDto> createLicenseType(@Valid @RequestBody LicenseTypeDto licenseTypeDto) {
        if (licenseTypeRepository.existsByName(licenseTypeDto.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "License type with name " + licenseTypeDto.getName() + " already exists");
        }

        LicenseType licenseType = new LicenseType();
        licenseType.setName(licenseTypeDto.getName());
        licenseType.setDefaultDurationInDays(licenseTypeDto.getDefaultDurationInDays());
        licenseType.setDescription(licenseTypeDto.getDescription());

        LicenseType savedLicenseType = licenseTypeRepository.save(licenseType);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedLicenseType));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseTypeDto> updateLicenseType(@PathVariable UUID id,
                                                            @Valid @RequestBody LicenseTypeDto licenseTypeDto) {
        LicenseType licenseType = licenseTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License type not found with id: " + id));

        if (!licenseType.getName().equals(licenseTypeDto.getName()) &&
                licenseTypeRepository.existsByName(licenseTypeDto.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "License type with name " + licenseTypeDto.getName() + " already exists");
        }

        licenseType.setName(licenseTypeDto.getName());
        licenseType.setDefaultDurationInDays(licenseTypeDto.getDefaultDurationInDays());
        licenseType.setDescription(licenseTypeDto.getDescription());

        LicenseType updatedLicenseType = licenseTypeRepository.save(licenseType);
        return ResponseEntity.ok(convertToDto(updatedLicenseType));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteLicenseType(@PathVariable UUID id) {
        if (!licenseTypeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "License type not found with id: " + id);
        }

        licenseTypeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "message", "License type deleted successfully",
                "id", id.toString()
        ));
    }

    @PatchMapping("/{id}/duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseTypeDto> updateDuration(@PathVariable UUID id,
                                                         @RequestBody Map<String, Integer> request) {
        Integer newDuration = request.get("defaultDurationInDays");
        if (newDuration == null || newDuration < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid duration (>=1) is required");
        }

        LicenseType licenseType = licenseTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License type not found with id: " + id));

        licenseType.setDefaultDurationInDays(newDuration);
        LicenseType updatedLicenseType = licenseTypeRepository.save(licenseType);
        return ResponseEntity.ok(convertToDto(updatedLicenseType));
    }
}