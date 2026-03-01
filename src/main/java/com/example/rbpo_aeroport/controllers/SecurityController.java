package com.example.rbpo_aeroport.controllers;

import com.example.rbpo_aeroport.entities.ApplicationUser;
import com.example.rbpo_aeroport.models.ApplicationUserDto;
import com.example.rbpo_aeroport.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SecurityController {
    @GetMapping("/csrf-token")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }
}
