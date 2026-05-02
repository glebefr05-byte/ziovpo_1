package com.licensing.service;

import com.licensing.controller.dto.*;
import com.licensing.entities.*;
import com.licensing.model.Ticket;
import com.licensing.repository.*;
import com.licensing.signature.SignatureKeyStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository historyRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final UserService userService;
    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final DeviceService deviceService;
    private final TicketService ticketService;
    private final SignatureKeyStoreService signatureKeyStoreService;

    @Value("${ticket.default-time-to-live:3600}")
    private Integer defaultTimeToLive;

    @Transactional
    public License createLicense(CreateLicenseRequest request, UUID adminId) throws Exception {
        Product product = productService.getProductOrFail(request.getProductId());
        LicenseType licenseType = licenseTypeService.getTypeOrFail(request.getTypeId());
        User owner = userService.getActiveUserOrFail(request.getOwnerId());
        User admin = userService.getUserById(adminId);

        License license = new License();
        license.setCode(generateLicenseCode());
        license.setProduct(product);
        license.setType(licenseType);
        license.setOwner(owner);
        license.setDeviceCount(request.getDeviceCount());
        license.setDescription(request.getDescription());
        license.setBlocked(false);

        License savedLicense = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(admin);
        history.setStatus("CREATED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription("License created by admin");
        historyRepository.save(history);

        return savedLicense;
    }

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, UUID userId) throws Exception {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new Exception("License not found with key: " + request.getActivationKey()));

        if (license.isBlocked()) {
            throw new Exception("License is blocked");
        }

        User currentUser = userService.getUserById(userId);

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new Exception("License already activated by another user");
        }

        Device device = deviceService.getOrCreateDevice(
                request.getDeviceMac(),
                request.getDeviceName(),
                currentUser
        );

        if (deviceLicenseRepository.existsByLicenseAndDeviceMacAddress(license, request.getDeviceMac())) {
            return createSignedTicketResponse(license, device, currentUser);
        }

        if (license.getUser() != null) {
            Integer activatedDevices = deviceLicenseRepository.countByLicenseId(license.getId());
            if (activatedDevices >= license.getDeviceCount()) {
                throw new Exception("Device limit reached for this license");
            }
        }

        boolean isFirstActivation = license.getUser() == null;

        if (isFirstActivation) {
            license.setUser(currentUser);
            license.setFirstActivationDate(LocalDate.now());
            license.setEndingDate(LocalDate.now().plusDays(license.getType().getDefaultDurationInDays()));
            licenseRepository.save(license);
        }

        DeviceLicense deviceLicense = new DeviceLicense();
        deviceLicense.setLicense(license);
        deviceLicense.setDevice(device);
        deviceLicense.setActivationDate(LocalDateTime.now());
        deviceLicenseRepository.save(deviceLicense);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(currentUser);
        history.setStatus("ACTIVATED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(isFirstActivation ?
                "First activation on device: " + device.getMacAddress() :
                "Additional activation on device: " + device.getMacAddress());
        historyRepository.save(history);

        return createSignedTicketResponse(license, device, currentUser);
    }

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, UUID userId) throws Exception {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new Exception("License not found with key: " + request.getActivationKey()));

        if (license.getUser() == null || !license.getUser().getId().equals(userId)) {
            throw new Exception("License does not belong to this user");
        }

        LocalDate now = LocalDate.now();
        boolean isRenewable = license.getEndingDate() == null ||
                license.getEndingDate().isBefore(now) ||
                license.getEndingDate().minusDays(7).isBefore(now);

        if (!isRenewable) {
            throw new Exception("License is not eligible for renewal");
        }

        LocalDate newEndingDate;
        if (license.getEndingDate() == null || license.getEndingDate().isBefore(now)) {
            newEndingDate = now.plusDays(license.getType().getDefaultDurationInDays());
        } else {
            newEndingDate = license.getEndingDate().plusDays(license.getType().getDefaultDurationInDays());
        }

        license.setEndingDate(newEndingDate);
        licenseRepository.save(license);

        User user = userService.getUserById(userId);
        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus("RENEWED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription("License renewed until: " + newEndingDate);
        historyRepository.save(history);

        Device device = getDeviceForLicense(license, userId);

        return createSignedTicketResponse(license, device, user);
    }

    public TicketResponse checkLicense(CheckLicenseRequest request, UUID userId) throws Exception {
        Device device = deviceService.findDeviceByMac(request.getDeviceMac());

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                request.getDeviceMac(),
                userId,
                request.getProductId(),
                LocalDate.now()
        ).orElseThrow(() -> new Exception("No active license found for this device and product"));

        User user = userService.getUserById(userId);

        return createSignedTicketResponse(license, device, user);
    }

    private TicketResponse createSignedTicketResponse(License license, Device device, User user) {
        try {
            Product product = license.getProduct();

            Ticket ticket = ticketService.createTicket(license, device, user, product);

            String signature = signatureKeyStoreService.sign(ticket.getDataForSigning());
            String algorithm = signatureKeyStoreService.getAlgorithm();

            return TicketResponse.fromTicketAndSignature(ticket, signature, algorithm);

        } catch (Exception e) {
            log.error("Failed to create signed ticket for license: {}", license.getCode(), e);
            throw new RuntimeException("Failed to create signed ticket", e);
        }
    }

    private Device getDeviceForLicense(License license, UUID userId) {
        return deviceLicenseRepository.findFirstByLicenseId(license.getId())
                .map(DeviceLicense::getDevice)
                .orElseThrow(() -> new RuntimeException("No device found for license"));
    }

    private String generateLicenseCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}