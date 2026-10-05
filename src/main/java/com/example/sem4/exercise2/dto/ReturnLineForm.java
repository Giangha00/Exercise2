package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnLineForm {

    @NotNull
    private Long detailId;

    @NotNull(message = "Nhập số lượng trả")
    @Min(value = 0, message = "Số lượng trả không được âm")
    private Integer quantity = 0;

    public ReturnLineForm(Long detailId, Integer quantity) {
        this.detailId = detailId;
        this.quantity = quantity;
    }
}
