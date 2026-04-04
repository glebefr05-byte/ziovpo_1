package com.licensing.controller.dto;

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

    public static TicketResponse fromTicketAndSignature(Ticket ticket, String signature) {
        return TicketResponse.builder()
                .ticket(ticket)
                .signature(signature)
                .build();
    }
}