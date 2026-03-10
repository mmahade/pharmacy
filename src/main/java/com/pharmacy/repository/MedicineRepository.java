package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<Medicine> findByPharmacyIdOrderByCreatedAtDesc(long pharmacyId);

    @Query("SELECT m FROM Medicine m JOIN m.masterMedicine bd WHERE m.pharmacy = :pharmacy AND (" +
            "LOWER(bd.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(bd.genericName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(bd.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(bd.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')))" +
            " ORDER BY bd.name ASC")
    List<Medicine> searchMedicines(@Param("pharmacy") Pharmacy pharmacy,
                                   @Param("search") String search,
                                   Pageable pageable);

    long countByPharmacyId(long pharmacyId);
}
