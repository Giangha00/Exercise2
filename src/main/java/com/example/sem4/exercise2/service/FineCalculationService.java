package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FineCalculationService implements FineCalculator {

    /**
     * lateDays tính phí = max(0, ngày trả - hạn trả - graceDays).
     * Phí lần này = lateDays × số lượng trả trong lần này × phí mỗi ngày.
     * Trần áp trên tổng phí của cả dòng, không chỉ riêng lần trả này.
     */
    @Override
    public FineQuote quote(LocalDate dueDate,
                           LocalDate actualReturnDate,
                           int returnedQuantity,
                           BigDecimal alreadyFinedOnLine,
                           FinePolicy policy) {
        if (policy == null) {
            throw new BusinessException("Chưa có chính sách phí phạt đang áp dụng");
        }
        if (returnedQuantity <= 0) {
            return new FineQuote(0, BigDecimal.ZERO);
        }
        long rawLateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, actualReturnDate));
        int graceDays = policy.getGraceDays() == null ? 0 : policy.getGraceDays();
        int chargeableDays = (int) Math.max(0, rawLateDays - graceDays);

        BigDecimal amount = policy.getDailyFineAmount()
                .multiply(BigDecimal.valueOf(chargeableDays))
                .multiply(BigDecimal.valueOf(returnedQuantity));

        BigDecimal maxFine = policy.getMaxFineAmount();
        if (maxFine != null && maxFine.signum() > 0) {
            BigDecimal already = alreadyFinedOnLine == null ? BigDecimal.ZERO : alreadyFinedOnLine;
            BigDecimal room = maxFine.subtract(already);
            if (room.signum() < 0) {
                room = BigDecimal.ZERO;
            }
            if (amount.compareTo(room) > 0) {
                amount = room;
            }
        }
        return new FineQuote(chargeableDays, amount.setScale(0, RoundingMode.HALF_UP));
    }
}
