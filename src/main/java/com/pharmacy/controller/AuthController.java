package com.pharmacy.controller;

import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterPharmacyRequest;
import com.pharmacy.dto.RegisterPharmacyResponse;
import com.pharmacy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Public endpoints for registration and login — no token required")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new pharmacy", description = "Creates a new pharmacy account and its first admin user. No token required.")
    @SecurityRequirements({})
    @PostMapping("/register-pharmacy")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterPharmacyResponse registerPharmacy(@Valid @RequestBody RegisterPharmacyRequest request) {
        return authService.registerPharmacy(request);
    }

    @Operation(summary = "Login and obtain JWT token", description = "Authenticates the user and returns a signed JWT. Pass this token as `Bearer <token>` on all secured endpoints.")
    @SecurityRequirements({})
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
