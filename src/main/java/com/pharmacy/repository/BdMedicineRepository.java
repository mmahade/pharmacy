package com.pharmacy.repository;

import com.pharmacy.entity.BdMedicine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BdMedicineRepository extends JpaRepository<BdMedicine, Long> {

    java.util.Optional<BdMedicine> findByNameIgnoreCaseAndManufacturerIgnoreCase(String name, String manufacturer);

    @Query("SELECT m FROM BdMedicine m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY m.name ASC")
    List<BdMedicine> searchMedicines(@Param("search") String search, Pageable pageable);

}
