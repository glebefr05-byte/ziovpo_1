package com.licensing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ticket {

    private UUID ticketId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime serverDateTime;

    private Integer timeToLive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime activationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime expirationDate;

    private UUID userId;

    private UUID deviceId;

    private boolean licenseBlocked;

    public boolean isTimeValid() {
        if (expiresAt == null || serverDateTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }

    public boolean isLicenseActive() {
        return !licenseBlocked;
    }

    public boolean isLicenseNotExpired() {
        if (expirationDate == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(expirationDate);
    }

    public boolean isValid() {
        return isTimeValid() && isLicenseActive() && isLicenseNotExpired();
    }

    @JsonIgnore
    public Map<String, Object> getDataForSigning() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("ticketId", ticketId != null ? ticketId.toString() : null);
        data.put("serverDateTime", serverDateTime != null ? serverDateTime.toString() : null);
        data.put("timeToLive", timeToLive);
        data.put("expiresAt", expiresAt != null ? expiresAt.toString() : null);
        data.put("activationDate", activationDate != null ? activationDate.toString() : null);
        data.put("expirationDate", expirationDate != null ? expirationDate.toString() : null);
        data.put("userId", userId != null ? userId.toString() : null);
        data.put("deviceId", deviceId != null ? deviceId.toString() : null);
        data.put("licenseBlocked", licenseBlocked);

        return data;
    }
}