# Architecture map

```text
Trình duyệt
  -> Controller (MVC)
    -> Service
      -> Repository
        -> MySQL (Flyway tạo schema)
      -> FineCalculationService (phí)
  <- Thymeleaf template
```

Một phiếu mượn (`borrowings`) thuộc một bạn đọc và có nhiều dòng (`borrowing_details`). Mỗi dòng trỏ một sách và số lượng. Tồn kho nằm trên `books.available_quantity`.

Phí không lưu riêng từng ngày. `FineCalculationService` đọc chính sách active trong `fine_policies`, rồi `BorrowingService` ghi `late_days`, `fine_amount` trên dòng và các cột tổng trên phiếu (`total`, `paid`, `waived`, `unpaid`).

Thanh toán vào `fine_payments`. Miễn giảm vào `fine_waivers`. Cả hai gắn `borrowing_id`.

Trạng thái phiếu: `BORROWING`, `PARTIALLY_RETURNED`, `RETURNED`, `OVERDUE`, `FINE_PENDING`, `FINE_PAID`, `CANCELLED`.

`books` và `borrowings` có `version` cho optimistic lock.

Test dùng H2. Chạy app dùng MySQL `library_management`.
