package com.example.sem4.exercise2.entity;

public enum BorrowingStatus {
    BORROWING("Đang mượn", "primary"),
    PARTIALLY_RETURNED("Trả một phần", "info"),
    RETURNED("Đã trả", "success"),
    OVERDUE("Quá hạn", "danger"),
    FINE_PENDING("Còn nợ phí", "warning"),
    FINE_PAID("Đã thanh toán phí", "success"),
    CANCELLED("Đã hủy", "secondary");

    private final String label;
    private final String badge;

    BorrowingStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
