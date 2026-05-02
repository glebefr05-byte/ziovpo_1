package com.licensing.controller;

import com.licensing.controller.dto.*;
import com.licensing.entities.License;
import com.licensing.entities.User;
import com.licensing.model.Ticket;
import com.licensing.repository.UserRepository;
import com.licensing.service.LicenseService;
import com.licensing.service.TicketService;
import com.licensing.signature.SignatureKeyStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final TicketService ticketService;
    private final SignatureKeyStoreService signatureKeyStoreService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new RuntimeException("Unable to get current user");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<License> createLicense(@Valid @RequestBody CreateLicenseRequest request) throws Exception {
        UUID adminId = getCurrentUserId();
        License createdLicense = licenseService.createLicense(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLicense);
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activateLicense(@Valid @RequestBody ActivateLicenseRequest request) throws Exception {
        UUID userId = getCurrentUserId();
        TicketResponse ticket = licenseService.activateLicense(request, userId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/renew")
    public ResponseEntity<TicketResponse> renewLicense(@Valid @RequestBody RenewLicenseRequest request) throws Exception {
        UUID userId = getCurrentUserId();
        TicketResponse ticket = licenseService.renewLicense(request, userId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/check")
    public ResponseEntity<TicketResponse> checkLicense(@Valid @RequestBody CheckLicenseRequest request) throws Exception {
        UUID userId = getCurrentUserId();
        TicketResponse ticket = licenseService.checkLicense(request, userId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of(
                "publicKey", signatureKeyStoreService.getPublicKeyBase64(),
                "algorithm", signatureKeyStoreService.getAlgorithm(),
                "certificateHash", signatureKeyStoreService.getCertificateHash()
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyTicket(@RequestBody TicketResponse ticketResponse) {
        if (ticketResponse == null || ticketResponse.getTicket() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", "Invalid ticket data"
            ));
        }

        boolean signatureValid = signatureKeyStoreService.verify(
                ticketResponse.getTicket().getDataForSigning(),
                ticketResponse.getSignature()
        );

        boolean ticketValid = ticketResponse.getTicket().isValid();

        return ResponseEntity.ok(Map.of(
                "signatureValid", signatureValid,
                "ticketValid", ticketValid,
                "overallValid", signatureValid && ticketValid,
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TicketResponse> refreshTicket(@RequestBody Ticket oldTicket) {
        if (oldTicket == null) {
            return ResponseEntity.badRequest().build();
        }

        Ticket newTicket = ticketService.refreshTicket(oldTicket);

        String signature = signatureKeyStoreService.sign(newTicket.getDataForSigning());
        String algorithm = signatureKeyStoreService.getAlgorithm();

        TicketResponse response = TicketResponse.fromTicketAndSignature(newTicket, signature, algorithm);

        return ResponseEntity.ok(response);
    }
}