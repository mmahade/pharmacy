package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, UUID> {
    List<PurchaseReturn> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    Optional<PurchaseReturn> findByIdAndPharmacy(UUID id, Pharmacy pharmacy);
}
