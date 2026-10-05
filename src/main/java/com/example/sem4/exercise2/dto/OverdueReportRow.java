package com.example.sem4.exercise2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueReportRow(
        Long borrowingId,
        String memberCode,
        String memberName,
        LocalDate dueDate,
        long lateDays,
        int unreturnedCopies,
        BigDecimal estimatedFine,
        BigDecimal unpaidFine
) {
}
