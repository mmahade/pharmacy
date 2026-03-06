package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<Medicine> findByPharmacyIdOrderByCreatedAtDesc(long pharmacyId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Medicine m WHERE m.pharmacy = :pharmacy AND (" +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") ORDER BY m.name ASC")
    List<Medicine> searchMedicines(@org.springframework.data.repository.query.Param("pharmacy") Pharmacy pharmacy,
                                   @org.springframework.data.repository.query.Param("search") String search,
                                   Pageable pageable);

    long countByPharmacyId(long pharmacyId);
}
