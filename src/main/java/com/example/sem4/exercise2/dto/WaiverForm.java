package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WaiverForm {

    @NotNull(message = "Nhập số tiền miễn giảm")
    @Positive(message = "Số tiền miễn giảm phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Lý do miễn giảm là bắt buộc")
    private String reason;

    @NotBlank(message = "Người duyệt là bắt buộc")
    private String approvedBy;
}
