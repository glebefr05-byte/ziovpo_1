package com.example.rbpo_aeroport.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationRequest {
    private String email;
    private String password;
    private String deviceId;
}
