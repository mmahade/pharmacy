package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {
    Optional<SaleTransaction> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

    List<SaleTransaction> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    List<SaleTransaction> findTop10ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    long countByPharmacy(Pharmacy pharmacy);

    long countByPharmacyAndSaleDateBetween(Pharmacy pharmacy, LocalDate startDate, LocalDate endDate);

    Optional<SaleTransaction> findTopByPharmacyAndSaleDateOrderByCreatedAtDesc(Pharmacy pharmacy, LocalDate saleDate);

    default BigDecimal totalForDay(Pharmacy pharmacy, LocalDate date) {
        return findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .filter(s -> date.equals(s.getSaleDate()))
                .map(SaleTransaction::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
