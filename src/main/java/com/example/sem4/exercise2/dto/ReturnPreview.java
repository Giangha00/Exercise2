package com.example.sem4.exercise2.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ReturnPreview {

    private final BigDecimal totalFine;
    private final List<Line> lines;

    public ReturnPreview(BigDecimal totalFine, List<Line> lines) {
        this.totalFine = totalFine;
        this.lines = lines;
    }

    @Getter
    public static class Line {
        private final String bookTitle;
        private final int quantity;
        private final int lateDays;
        private final BigDecimal fine;

        public Line(String bookTitle, int quantity, int lateDays, BigDecimal fine) {
            this.bookTitle = bookTitle;
            this.quantity = quantity;
            this.lateDays = lateDays;
            this.fine = fine;
        }
    }
}
