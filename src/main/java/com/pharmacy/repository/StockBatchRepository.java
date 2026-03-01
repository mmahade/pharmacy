package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {
    /**
     * FIFO: soonest expiry first (use these first when selling).
     */
    List<StockBatch> findByMedicineOrderByExpiryDateAsc(Medicine medicine);

    Optional<StockBatch> findByMedicineAndBatchNumber(Medicine medicine, String batchNumber);

    Optional<StockBatch> findByMedicineAndExpiryDate(Medicine medicine, LocalDate expiryDate);

    /**
     * Batches expiring between start and end (inclusive), for pharmacy's medicines,
     * ordered by expiry.
     */
    List<StockBatch> findByMedicine_PharmacyAndExpiryDateBetweenOrderByExpiryDateAsc(
            Pharmacy pharmacy, LocalDate startInclusive, LocalDate endInclusive);
}
