package com.example.sem4.exercise2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberForm {

    private Long id;

    @NotBlank(message = "Nhập mã độc giả")
    private String memberCode;

    @NotBlank(message = "Nhập họ tên")
    private String fullName;

    @NotBlank(message = "Nhập email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Nhập số điện thoại")
    private String phone;
}
