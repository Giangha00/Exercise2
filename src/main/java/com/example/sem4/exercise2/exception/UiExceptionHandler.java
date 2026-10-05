package com.example.sem4.exercise2.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public String conflict(OptimisticLockingFailureException ex, Model model) {
        model.addAttribute("error", "Dữ liệu vừa được người khác cập nhật. Thao tác đã được hoàn tác, vui lòng thử lại.");
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integrity(DataIntegrityViolationException ex, Model model) {
        model.addAttribute("error", "Dữ liệu vi phạm ràng buộc. Thao tác đã được hoàn tác.");
        return "error";
    }
}
