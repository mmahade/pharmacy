package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseOrder;
import com.pharmacy.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    Optional<PurchaseOrder> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<PurchaseOrder> findByPharmacyAndStatusOrderByCreatedAtDesc(Pharmacy pharmacy, PurchaseOrderStatus status);

    List<PurchaseOrder> findByPharmacyAndOrderDateBetweenOrderByOrderDateDesc(Pharmacy pharmacy, LocalDate start,
            LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.totalAmount - p.amountPaid) FROM PurchaseOrder p WHERE p.pharmacy = :pharmacy")
    java.math.BigDecimal totalPendingBalance(@org.springframework.data.repository.query.Param("pharmacy") Pharmacy pharmacy);

    long countByPharmacy(Pharmacy pharmacy);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.totalAmount) FROM PurchaseOrder p WHERE p.pharmacy = :pharmacy AND p.orderDate BETWEEN :start AND :end")
    java.math.BigDecimal totalForRange(Pharmacy pharmacy, java.time.LocalDate start, java.time.LocalDate end);

    default java.math.BigDecimal totalForDay(Pharmacy pharmacy, java.time.LocalDate date) {
        java.math.BigDecimal total = totalForRange(pharmacy, date, date);
        return total != null ? total : java.math.BigDecimal.ZERO;
    }
}
