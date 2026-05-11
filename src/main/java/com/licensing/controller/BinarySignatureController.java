package com.licensing.controller;

import com.licensing.binary.MultipartMixedResponseFactory;
import com.licensing.controller.dto.SignaturesByIdsRequest;
import com.licensing.service.BinarySignatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/binary/signatures")
@RequiredArgsConstructor
public class BinarySignatureController {

    private final BinarySignatureService binarySignatureService;
    private final MultipartMixedResponseFactory multipartFactory;

    @GetMapping("/full")
    public ResponseEntity<MultiValueMap<String, Object>> getFullDatabase() {
        try {
            log.info("Processing full database export request");

            byte[] manifestBytes = binarySignatureService.getFullManifest();
            byte[] dataBytes = binarySignatureService.getFullData();

            log.info("Full export completed: manifest size={}, data size={}",
                    manifestBytes.length, dataBytes.length);

            return multipartFactory.create(manifestBytes, dataBytes);

        } catch (Exception e) {
            log.error("Failed to export full database", e);
            throw new RuntimeException("Failed to export full database", e);
        }
    }

    @GetMapping("/increment")
    public ResponseEntity<MultiValueMap<String, Object>> getIncrement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {

        if (since == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            log.info("Processing increment export request: since={}", since);

            byte[] manifestBytes = binarySignatureService.getIncrementManifest(since);
            byte[] dataBytes = binarySignatureService.getIncrementData(since);

            log.info("Increment export completed: manifest size={}, data size={}",
                    manifestBytes.length, dataBytes.length);

            return multipartFactory.create(manifestBytes, dataBytes);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid since parameter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to export increment", e);
            throw new RuntimeException("Failed to export increment", e);
        }
    }

    @PostMapping("/by-ids")
    public ResponseEntity<MultiValueMap<String, Object>> getSignaturesByIds(
            @Valid @RequestBody SignaturesByIdsRequest request) {

        try {
            List<UUID> ids = request.getIds();
            log.info("Processing by-ids export request: {} ids", ids.size());

            byte[] manifestBytes = binarySignatureService.getByIdsManifest(ids);
            byte[] dataBytes = binarySignatureService.getByIdsData(ids);

            log.info("By-ids export completed: manifest size={}, data size={}",
                    manifestBytes.length, dataBytes.length);

            return multipartFactory.create(manifestBytes, dataBytes);

        } catch (Exception e) {
            log.error("Failed to export signatures by ids", e);
            throw new RuntimeException("Failed to export signatures by ids", e);
        }
    }
}