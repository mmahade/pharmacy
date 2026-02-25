package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findByIdAndPharmacy(UUID id, Pharmacy pharmacy);
    List<Prescription> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    long countByPharmacy(Pharmacy pharmacy);
    List<Prescription> findTop5ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    long countByPharmacyAndPrescriptionDateBetween(Pharmacy pharmacy, LocalDate startDate, LocalDate endDate);
}
