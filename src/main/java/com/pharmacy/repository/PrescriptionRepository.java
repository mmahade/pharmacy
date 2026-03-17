package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<Prescription> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    Page<Prescription> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy, Pageable pageable);

    long countByPharmacyId(long pharmacy);

    long countByPharmacyAndStatus(Pharmacy pharmacy, PrescriptionStatus status);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Prescription p WHERE p.pharmacy = :pharmacy")
    BigDecimal sumTotalValueByPharmacy(Pharmacy pharmacy);

    List<Prescription> findTop5ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    long countByPharmacyAndPrescriptionDateBetween(Pharmacy pharmacy, LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacy = :pharmacy AND " +
            "(LOWER(p.prescriptionNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.patientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.doctorName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Prescription> searchByPharmacy(Pharmacy pharmacy, String query);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacy = :pharmacy AND " +
            "(LOWER(p.prescriptionNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.patientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.doctorName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Prescription> searchByPharmacy(Pharmacy pharmacy, String query, Pageable pageable);
}
