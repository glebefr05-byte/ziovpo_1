package com.licensing.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.licensing.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {

    private Ticket ticket;

    private String signature;

    @JsonIgnore
    private String signatureAlgorithm;

    private String certificateHash;

    @Builder.Default
    private LocalDateTime signatureTimestamp = LocalDateTime.now();

    public static TicketResponse fromTicketAndSignature(Ticket ticket, String signature, String algorithm) {
        return TicketResponse.builder()
                .ticket(ticket)
                .signature(signature)
                .signatureAlgorithm(algorithm)
                .signatureTimestamp(LocalDateTime.now())
                .build();
    }
}