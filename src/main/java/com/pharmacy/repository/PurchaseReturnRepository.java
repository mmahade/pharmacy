package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    List<PurchaseReturn> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    Optional<PurchaseReturn> findByIdAndPharmacy(Long id, Pharmacy pharmacy);
}
