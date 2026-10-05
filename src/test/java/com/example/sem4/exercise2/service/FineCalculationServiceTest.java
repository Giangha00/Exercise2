package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.entity.FinePolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FineCalculationServiceTest {

    private final FineCalculationService calculator = new FineCalculationService();

    @Test
    void onTimeReturnHasNoFine() {
        FineCalculator.FineQuote quote = calculator.quote(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 10),
                1,
                BigDecimal.ZERO,
                policy(0, "5000", null));

        assertEquals(0, quote.lateDays());
        assertEquals(0, quote.amount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void threeDaysLateOneCopy() {
        FineCalculator.FineQuote quote = calculator.quote(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                1,
                BigDecimal.ZERO,
                policy(0, "5000", null));

        assertEquals(3, quote.lateDays());
        assertEquals(0, quote.amount().compareTo(new BigDecimal("15000")));
    }

    @Test
    void threeDaysLateTwoCopies() {
        FineCalculator.FineQuote quote = calculator.quote(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                2,
                BigDecimal.ZERO,
                policy(0, "5000", null));

        assertEquals(0, quote.amount().compareTo(new BigDecimal("30000")));
    }

    @Test
    void graceDaysReduceChargeableDays() {
        FineCalculator.FineQuote quote = calculator.quote(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                1,
                BigDecimal.ZERO,
                policy(1, "5000", null));

        assertEquals(1, quote.lateDays());
        assertEquals(0, quote.amount().compareTo(new BigDecimal("5000")));
    }

    @Test
    void maxFineCapsTheLine() {
        FineCalculator.FineQuote quote = calculator.quote(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                1,
                BigDecimal.ZERO,
                policy(0, "5000", "10000"));

        assertEquals(0, quote.amount().compareTo(new BigDecimal("10000")));
    }

    private FinePolicy policy(int graceDays, String daily, String max) {
        FinePolicy policy = new FinePolicy();
        policy.setDailyFineAmount(new BigDecimal(daily));
        policy.setGraceDays(graceDays);
        policy.setMaxFineAmount(max == null ? null : new BigDecimal(max));
        policy.setActive(true);
        return policy;
    }
}
