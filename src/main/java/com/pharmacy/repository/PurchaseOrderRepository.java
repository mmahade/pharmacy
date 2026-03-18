package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseOrder;
import com.pharmacy.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Page<PurchaseOrder> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy, Pageable pageable);

    List<PurchaseOrder> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    Optional<PurchaseOrder> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    @Query("SELECT p FROM PurchaseOrder p WHERE p.pharmacy = :pharmacy " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR LOWER(p.orderNumber) LIKE :search " +
            "OR LOWER(p.supplier.name) LIKE :search) " +
            "ORDER BY p.createdAt DESC")
    Page<PurchaseOrder> search(@Param("pharmacy") Pharmacy pharmacy,
                               @Param("status") PurchaseOrderStatus status,
                               @Param("search") String search,
                               Pageable pageable);

    List<PurchaseOrder> findByPharmacyAndStatusOrderByCreatedAtDesc(Pharmacy pharmacy, PurchaseOrderStatus status);

    List<PurchaseOrder> findByPharmacyAndOrderDateBetweenOrderByOrderDateDesc(Pharmacy pharmacy, LocalDate start,
            LocalDate end);

    @Query("SELECT SUM(p.totalAmount - p.amountPaid) FROM PurchaseOrder p WHERE p.pharmacy = :pharmacy")
    BigDecimal totalPendingBalance(@Param("pharmacy") Pharmacy pharmacy);

    long countByPharmacy(Pharmacy pharmacy);

    @Query("SELECT SUM(p.totalAmount) FROM PurchaseOrder p WHERE p.pharmacy = :pharmacy AND p.orderDate BETWEEN :start AND :end")
    BigDecimal totalForRange(Pharmacy pharmacy, LocalDate start, LocalDate end);

    default BigDecimal totalForDay(Pharmacy pharmacy, LocalDate date) {
        BigDecimal total = totalForRange(pharmacy, date, date);
        return total != null ? total : BigDecimal.ZERO;
    }
}
