package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByPharmacyOrderByNameAsc(Pharmacy pharmacy);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Supplier s WHERE s.pharmacy = :pharmacy AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") ORDER BY s.name ASC")
    List<Supplier> searchSuppliers(@org.springframework.data.repository.query.Param("pharmacy") Pharmacy pharmacy,
                                   @org.springframework.data.repository.query.Param("search") String search);
}
