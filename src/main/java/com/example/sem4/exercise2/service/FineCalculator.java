package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.entity.FinePolicy;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Điểm mở rộng khi thư viện đổi cách tính phí theo thể loại sách:
 * thêm implementation mới, không sửa công thức trong controller.
 */
public interface FineCalculator {

    FineQuote quote(LocalDate dueDate,
                    LocalDate actualReturnDate,
                    int returnedQuantity,
                    BigDecimal alreadyFinedOnLine,
                    FinePolicy policy);

    record FineQuote(int lateDays, BigDecimal amount) {
    }
}
