package com.example.sem4.exercise2.exception;

/**
 * Lỗi nghiệp vụ. Kế thừa RuntimeException để @Transactional hoàn tác cả giao dịch.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
