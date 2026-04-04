package com.licensing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    private String userEmail;

    private String userName;

    private UUID deviceId;

    private String deviceMacAddress;

    private String deviceName;

    private boolean licenseBlocked;

    private String licenseCode;

    private String licenseType;

    private String productName;

    private UUID productId;

    private Integer deviceLimit;

    private Integer activatedDevicesCount;

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
}