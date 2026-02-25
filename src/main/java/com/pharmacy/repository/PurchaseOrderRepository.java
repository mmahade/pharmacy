package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseOrder;
import com.pharmacy.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    Optional<PurchaseOrder> findByIdAndPharmacy(UUID id, Pharmacy pharmacy);
    List<PurchaseOrder> findByPharmacyAndStatusOrderByCreatedAtDesc(Pharmacy pharmacy, PurchaseOrderStatus status);
}
