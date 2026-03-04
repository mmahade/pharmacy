package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
    List<SaleReturn> findBySale_PharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    java.util.Optional<SaleReturn> findByIdAndSale_Pharmacy(Long id, Pharmacy pharmacy);

    List<SaleReturn> findBySale_PharmacyAndReturnDateBetweenOrderByReturnDateDesc(Pharmacy pharmacy, LocalDate start,
            LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(r.totalAmount) FROM SaleReturn r WHERE r.sale.pharmacy = :pharmacy AND r.returnDate BETWEEN :start AND :end")
    java.math.BigDecimal totalForRange(Pharmacy pharmacy, java.time.LocalDate start, java.time.LocalDate end);
}
