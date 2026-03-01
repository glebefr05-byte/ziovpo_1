package com.example.rbpo_aeroport.controllers;

import com.example.rbpo_aeroport.models.ApplicationUserDto;
import com.example.rbpo_aeroport.services.JwtTokenService;
import com.example.rbpo_aeroport.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.rbpo_aeroport.entities.ApplicationUser;
import com.example.rbpo_aeroport.models.AuthenticationRequest;
import com.example.rbpo_aeroport.models.AuthenticationResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            AuthenticationResponse response = tokenService.generateTokenPair(authentication, request.getDeviceId());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            AuthenticationResponse response = tokenService.refreshTokens(refreshToken);
            return ResponseEntity.ok(response);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody ApplicationUserDto request) {
        try {
            ApplicationUser user = userDetailsService.registerUser(request);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "userId", user.getId(),
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
