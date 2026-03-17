package com.pharmacy.dto;

import java.math.BigDecimal;

public record PrescriptionStatsResponse(
        long totalCount,
        long pendingCount,
        long completedCount,
        BigDecimal totalValue
) {}
