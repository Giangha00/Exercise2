package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentForm {

    @NotNull(message = "Nhập số tiền")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Chọn hình thức thanh toán")
    private String method;

    private String note;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;
}
