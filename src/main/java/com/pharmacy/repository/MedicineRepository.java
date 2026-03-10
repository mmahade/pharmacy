package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

        Optional<Medicine> findByIdAndPharmacy(Long id, Pharmacy pharmacy);

        List<Medicine> findByPharmacyIdOrderByCreatedAtDesc(long pharmacyId);

        @EntityGraph(attributePaths = { "masterMedicine" })
        Page<Medicine> findByPharmacyId(long pharmacyId, Pageable pageable);

        @Query("""
                        SELECT m FROM Medicine m
                        JOIN m.masterMedicine bd
                        WHERE m.pharmacy = :pharmacy
                        AND (
                            LOWER(bd.name) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(bd.genericName) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(bd.category) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(bd.manufacturer) LIKE LOWER(CONCAT('%', :search, '%'))
                        )
                        ORDER BY bd.name ASC
                        """)
        Page<Medicine> searchMedicines(
                        @Param("pharmacy") Pharmacy pharmacy,
                        @Param("search") String search,
                        Pageable pageable);

        long countByPharmacyId(long pharmacyId);

        @Query("""
                        SELECT m
                        FROM Medicine m
                        JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        AND b.expiryDate >= CURRENT_DATE
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) <= m.minStock
                        AND COALESCE(SUM(b.quantity),0) > 0
                        """)
        Page<Medicine> findLowStockMedicines(
                        @Param("pharmacy") Pharmacy pharmacy,
                        Pageable pageable);

        @Query("""
                        SELECT m
                        FROM Medicine m
                        LEFT JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) = 0
                        """)
        Page<Medicine> findOutOfStockMedicines(
                        @Param("pharmacy") Pharmacy pharmacy,
                        Pageable pageable);

        @Query("""
                        SELECT m
                        FROM Medicine m
                        LEFT JOIN m.batches b ON b.medicine = m AND b.expiryDate >= CURRENT_DATE
                        WHERE m.pharmacy = :pharmacy
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) <= m.minStock
                        """)
        Page<Medicine> findBelowThresholdMedicines(
                        @Param("pharmacy") Pharmacy pharmacy,
                        Pageable pageable);

        @Query("""
                        SELECT m
                        FROM Medicine m
                        JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        AND b.expiryDate >= CURRENT_DATE
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) > m.minStock
                        """)
        Page<Medicine> findInStockMedicines(
                        @Param("pharmacy") Pharmacy pharmacy,
                        Pageable pageable);

        @Query("""
                        SELECT COUNT(m)
                        FROM Medicine m
                        JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        AND b.expiryDate >= CURRENT_DATE
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) > m.minStock
                        """)
        List<Long> countInStockMedicines(@Param("pharmacy") Pharmacy pharmacy);

        @Query("""
                        SELECT COUNT(m)
                        FROM Medicine m
                        JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        AND b.expiryDate >= CURRENT_DATE
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) <= m.minStock
                        AND COALESCE(SUM(b.quantity),0) > 0
                        """)
        List<Long> countLowStockMedicines(@Param("pharmacy") Pharmacy pharmacy);

        @Query("""
                        SELECT COUNT(m)
                        FROM Medicine m
                        LEFT JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) = 0
                        """)
        List<Long> countOutOfStockMedicines(@Param("pharmacy") Pharmacy pharmacy);

        @Query("""
                        SELECT COUNT(m)
                        FROM Medicine m
                        LEFT JOIN m.batches b ON b.medicine = m AND b.expiryDate >= CURRENT_DATE
                        WHERE m.pharmacy = :pharmacy
                        GROUP BY m
                        HAVING COALESCE(SUM(b.quantity),0) <= m.minStock
                        """)
        List<Long> countBelowThresholdMedicines(@Param("pharmacy") Pharmacy pharmacy);

        @Query("""
                        SELECT SUM(b.quantity), SUM(b.quantity * m.price)
                        FROM Medicine m
                        JOIN m.batches b
                        WHERE m.pharmacy = :pharmacy
                        AND b.expiryDate >= CURRENT_DATE
                        """)
        List<Object[]> getInventoryAggregation(@Param("pharmacy") Pharmacy pharmacy);
}