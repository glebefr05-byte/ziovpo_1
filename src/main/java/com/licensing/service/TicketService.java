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
                .deviceId(device.getId())
                .licenseBlocked(license.isBlocked())
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
                .deviceId(oldTicket.getDeviceId())
                .licenseBlocked(oldTicket.isLicenseBlocked())
                .build();
    }
}