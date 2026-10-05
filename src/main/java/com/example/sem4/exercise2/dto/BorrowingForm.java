package com.example.sem4.exercise2.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BorrowingForm {

    @NotNull(message = "Chọn độc giả")
    private Long memberId;

    @NotNull(message = "Chọn hạn trả")
    @FutureOrPresent(message = "Hạn trả không được ở trong quá khứ")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotEmpty(message = "Phiếu mượn phải có ít nhất một đầu sách")
    private List<@Valid BorrowLineForm> lines = new ArrayList<>();
}
