package com.pharmacy.controller;

import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterPharmacyRequest;
import com.pharmacy.dto.RegisterPharmacyResponse;
import com.pharmacy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-pharmacy")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterPharmacyResponse registerPharmacy(@Valid @RequestBody RegisterPharmacyRequest request) {
        return authService.registerPharmacy(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
