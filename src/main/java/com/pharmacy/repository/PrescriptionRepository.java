package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<Prescription> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    long countByPharmacyId(long pharmacy);

    List<Prescription> findTop5ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    long countByPharmacyAndPrescriptionDateBetween(Pharmacy pharmacy, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Prescription p WHERE p.pharmacy = :pharmacy AND " +
            "(LOWER(p.prescriptionNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.patientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.doctorName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Prescription> searchByPharmacy(Pharmacy pharmacy, String query);
}
