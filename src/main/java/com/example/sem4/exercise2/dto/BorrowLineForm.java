package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowLineForm {

    @NotNull(message = "Chọn sách")
    private Long bookId;

    @NotNull(message = "Nhập số lượng")
    @Min(value = 1, message = "Số lượng mượn phải lớn hơn 0")
    private Integer quantity;
}
