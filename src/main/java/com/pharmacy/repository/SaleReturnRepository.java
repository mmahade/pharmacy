package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
    List<SaleReturn> findBySale_PharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
}
