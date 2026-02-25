package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID> {
    Optional<Pharmacy> findByEmailIgnoreCase(String email);
    Optional<Pharmacy> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);
}
