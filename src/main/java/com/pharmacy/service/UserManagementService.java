package com.pharmacy.service;

import com.pharmacy.dto.CreateUserRequest;
import com.pharmacy.dto.UserResponse;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.PharmacyRepository;
import com.pharmacy.repository.UserAccountRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserAccountRepository userAccountRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUserUnderSamePharmacy(AppUserPrincipal currentUser, CreateUserRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User email already exists");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(currentUser.getPharmacyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));

        UserAccount user = new UserAccount();
        user.setPharmacy(pharmacy);
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        UserAccount saved = userAccountRepository.save(user);
        return toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listPharmacyUsers(AppUserPrincipal currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(currentUser.getPharmacyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));

        return userAccountRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getPharmacy().getId()
        );
    }
}
