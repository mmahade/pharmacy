package com.pharmacy.dto;

import java.math.BigDecimal;

public record PrescriptionStats(
    long totalCount,
    long pendingCount,
    long completedCount,
    BigDecimal totalValue
) {}
