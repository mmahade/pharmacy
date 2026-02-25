package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, UUID> {
    List<SaleReturn> findBySale_PharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
}
