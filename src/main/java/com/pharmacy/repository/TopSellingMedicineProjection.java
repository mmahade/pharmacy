package com.pharmacy.repository;

public interface TopSellingMedicineProjection {
    String getMedicineName();

    Long getTotalQuantity();

    java.math.BigDecimal getTotalRevenue();
}
