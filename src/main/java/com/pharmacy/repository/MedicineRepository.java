package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<Medicine> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    List<Medicine> findByPharmacyAndNameContainingIgnoreCaseOrderByNameAsc(Pharmacy pharmacy, String search,
                                                                           Pageable pageable);

    long countByPharmacy(Pharmacy pharmacy);
}
