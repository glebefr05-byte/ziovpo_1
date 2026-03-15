package com.licensing.controller.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TicketResponse {
    private String licenseCode;
    private String productName;
    private String licenseType;
    private LocalDate firstActivationDate;
    private LocalDate endingDate;
    private boolean blocked;
    private Integer deviceCount;
    private Integer activatedDevices;
}