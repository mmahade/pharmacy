package com.pharmacy.service;

import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.RegisterPharmacyRequest;
import com.pharmacy.dto.RegisterPharmacyResponse;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Role;
import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.PharmacyRepository;
import com.pharmacy.repository.UserAccountRepository;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PharmacyRepository pharmacyRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterPharmacyResponse registerPharmacy(RegisterPharmacyRequest request) {
        if (pharmacyRepository.existsByNameIgnoreCase(request.pharmacyName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pharmacy name already exists");
        }
        if (pharmacyRepository.existsByEmailIgnoreCase(request.pharmacyEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pharmacy email already exists");
        }
        if (pharmacyRepository.existsByLicenseNumberIgnoreCase(request.licenseNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already exists");
        }
        if (userAccountRepository.existsByEmailIgnoreCase(request.adminEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admin email already exists");
        }

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(request.pharmacyName().trim());
        pharmacy.setEmail(request.pharmacyEmail().trim().toLowerCase());
        pharmacy.setLicenseNumber(request.licenseNumber().trim());
        pharmacy = pharmacyRepository.save(pharmacy);

        UserAccount admin = new UserAccount();
        admin.setPharmacy(pharmacy);
        admin.setFullName(request.adminFullName().trim());
        admin.setEmail(request.adminEmail().trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin = userAccountRepository.save(admin);

        return new RegisterPharmacyResponse(
                pharmacy.getId(),
                pharmacy.getName(),
                admin.getId(),
                admin.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = jwtService.generateToken(principal);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getPharmacy().getId(),
                user.getPharmacy().getName()
        );
    }
}
