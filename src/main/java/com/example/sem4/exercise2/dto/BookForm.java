package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm {

    private Long id;

    @NotBlank(message = "Nhập tên sách")
    private String title;

    @NotBlank(message = "Nhập tác giả")
    private String author;

    @NotBlank(message = "Nhập ISBN")
    private String isbn;

    @NotNull(message = "Nhập tồn kho")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer availableQuantity;

    @NotNull(message = "Chọn thể loại")
    private Long categoryId;
}
