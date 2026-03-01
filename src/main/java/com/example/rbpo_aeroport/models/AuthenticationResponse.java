package com.example.rbpo_aeroport.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String email;
    private String accessToken;
    private String refreshToken;
}
