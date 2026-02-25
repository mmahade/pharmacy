package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByPharmacyOrderByNameAsc(Pharmacy pharmacy);
}
