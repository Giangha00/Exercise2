package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FinePolicyForm {

    @NotNull(message = "Nhập phí mỗi ngày")
    @Positive(message = "Phí mỗi ngày phải lớn hơn 0")
    private BigDecimal dailyFineAmount;

    @NotNull(message = "Nhập số ngày miễn phạt")
    @Min(value = 0, message = "Số ngày miễn phạt không được âm")
    private Integer graceDays;

    @PositiveOrZero(message = "Trần phí không được âm")
    private BigDecimal maxFineAmount;
}
