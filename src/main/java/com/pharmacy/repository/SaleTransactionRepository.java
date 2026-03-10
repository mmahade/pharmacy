package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {
    Optional<SaleTransaction> findByIdAndPharmacy(Long id, Pharmacy pharmacy);
    Optional<SaleTransaction> findByPrescription(com.pharmacy.entity.Prescription prescription);

    List<SaleTransaction> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    List<SaleTransaction> findTop10ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    long countByPharmacy(Pharmacy pharmacy);

    long countByPharmacyAndSaleDateBetween(Pharmacy pharmacy, LocalDate startDate, LocalDate endDate);

    Optional<SaleTransaction> findTopByPharmacyAndSaleDateOrderByCreatedAtDesc(Pharmacy pharmacy, LocalDate saleDate);

    List<SaleTransaction> findByPharmacyAndSaleDateBetweenOrderBySaleDateDesc(Pharmacy pharmacy, LocalDate start,
            LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(s.total) FROM SaleTransaction s WHERE s.pharmacy = :pharmacy AND s.saleDate BETWEEN :start AND :end")
    BigDecimal totalForRange(Pharmacy pharmacy, LocalDate start, LocalDate end);

    default BigDecimal totalForDay(Pharmacy pharmacy, LocalDate date) {
        BigDecimal total = totalForRange(pharmacy, date, date);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Query("SELECT SUM(s.total - s.amountPaid) FROM SaleTransaction s WHERE s.pharmacy = :pharmacy")
    BigDecimal totalPendingBalance(Pharmacy pharmacy);

    @org.springframework.data.jpa.repository.Query("SELECT " +
            "si.medicine.masterMedicine.name AS medicineName, " +
            "SUM(si.quantity) AS totalQuantity, " +
            "SUM(si.lineTotal) AS totalRevenue " +
            "FROM SaleItem si " +
            "WHERE si.sale.pharmacy = :pharmacy " +
            "GROUP BY si.medicine.id, si.medicine.masterMedicine.name " +
            "ORDER BY SUM(si.quantity) DESC")
    List<TopSellingMedicineProjection> findTopSellingMedicines(
            @org.springframework.data.repository.query.Param("pharmacy") Pharmacy pharmacy,
            org.springframework.data.domain.Pageable pageable);
}
