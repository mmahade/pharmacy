package com.pharmacy.service;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.PharmacyRepository;
import com.pharmacy.repository.UserAccountRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TenantAccessService {

    private final PharmacyRepository pharmacyRepository;
    private final UserAccountRepository userAccountRepository;

    public Pharmacy currentPharmacy(AppUserPrincipal principal) {
        return pharmacyRepository.findById(principal.getPharmacyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
    }

    public UserAccount currentUser(AppUserPrincipal principal) {
        Pharmacy pharmacy = currentPharmacy(principal);
        return userAccountRepository.findByIdAndPharmacy(principal.getUserId(), pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
