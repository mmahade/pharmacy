package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Optional<Medicine> findByIdAndPharmacy(UUID id, Pharmacy pharmacy);
    List<Medicine> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    List<Medicine> findByPharmacyAndNameContainingIgnoreCaseOrderByNameAsc(Pharmacy pharmacy, String search, Pageable pageable);
    long countByPharmacy(Pharmacy pharmacy);
}
