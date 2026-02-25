package com.pharmacy.service;

import com.pharmacy.dto.SupplierRequest;
import com.pharmacy.dto.SupplierResponse;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Supplier;
import com.pharmacy.repository.SupplierRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final TenantAccessService tenantAccessService;

    public List<SupplierResponse> list(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return supplierRepository.findByPharmacyOrderByNameAsc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse get(AppUserPrincipal principal, UUID id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getPharmacy().getId().equals(pharmacy.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        return toResponse(supplier);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public SupplierResponse create(AppUserPrincipal principal, SupplierRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Supplier supplier = new Supplier();
        supplier.setPharmacy(pharmacy);
        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null);
        supplier.setEmail(request.email() != null ? request.email().trim() : null);
        supplier.setPhone(request.phone() != null ? request.phone().trim() : null);
        supplier.setAddress(request.address() != null ? request.address().trim() : null);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public SupplierResponse update(AppUserPrincipal principal, UUID id, SupplierRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getPharmacy().getId().equals(pharmacy.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null);
        supplier.setEmail(request.email() != null ? request.email().trim() : null);
        supplier.setPhone(request.phone() != null ? request.phone().trim() : null);
        supplier.setAddress(request.address() != null ? request.address().trim() : null);
        return toResponse(supplierRepository.save(supplier));
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(
                s.getId(),
                s.getName(),
                s.getContactPerson(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress(),
                s.getCreatedAt()
        );
    }
}
