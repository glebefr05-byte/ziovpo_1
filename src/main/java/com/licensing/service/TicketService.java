package com.licensing.service;

import com.licensing.entities.*;
import com.licensing.model.Ticket;
import com.licensing.repository.DeviceLicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final DeviceLicenseRepository deviceLicenseRepository;

    @Value("${ticket.default-time-to-live:3600}")
    private Integer defaultTimeToLive;

    public Ticket createTicket(License license, Device device, User user, Product product) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(defaultTimeToLive);

        Integer activatedDevicesCount = deviceLicenseRepository.countByLicenseId(license.getId());

        return Ticket.builder()
                .ticketId(UUID.randomUUID())
                .serverDateTime(now)
                .timeToLive(defaultTimeToLive)
                .expiresAt(expiresAt)
                .activationDate(license.getFirstActivationDate() != null ?
                        license.getFirstActivationDate().atStartOfDay() : null)
                .expirationDate(license.getEndingDate() != null ?
                        license.getEndingDate().atStartOfDay() : null)
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getName())
                .deviceId(device.getId())
                .deviceMacAddress(device.getMacAddress())
                .deviceName(device.getName())
                .licenseBlocked(license.isBlocked())
                .licenseCode(license.getCode())
                .licenseType(license.getType() != null ? license.getType().getName() : null)
                .productName(product != null ? product.getName() : null)
                .productId(product != null ? product.getId() : null)
                .deviceLimit(license.getDeviceCount())
                .activatedDevicesCount(activatedDevicesCount)
                .build();
    }

    public Ticket refreshTicket(Ticket oldTicket) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpiresAt = now.plusSeconds(defaultTimeToLive);

        return Ticket.builder()
                .ticketId(UUID.randomUUID())
                .serverDateTime(now)
                .timeToLive(defaultTimeToLive)
                .expiresAt(newExpiresAt)
                .activationDate(oldTicket.getActivationDate())
                .expirationDate(oldTicket.getExpirationDate())
                .userId(oldTicket.getUserId())
                .userEmail(oldTicket.getUserEmail())
                .userName(oldTicket.getUserName())
                .deviceId(oldTicket.getDeviceId())
                .deviceMacAddress(oldTicket.getDeviceMacAddress())
                .deviceName(oldTicket.getDeviceName())
                .licenseBlocked(oldTicket.isLicenseBlocked())
                .licenseCode(oldTicket.getLicenseCode())
                .licenseType(oldTicket.getLicenseType())
                .productName(oldTicket.getProductName())
                .productId(oldTicket.getProductId())
                .deviceLimit(oldTicket.getDeviceLimit())
                .activatedDevicesCount(oldTicket.getActivatedDevicesCount())
                .build();
    }
}