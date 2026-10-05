# Database

Schema gốc: `src/main/resources/db/migration/V1__init_schema.sql`.

MySQL, database `library_management`, charset `utf8mb4`. Hibernate `ddl-auto=validate`. Mọi đổi bảng là file `V{n}__*.sql` mới.

## Bảng

- `categories` — tên duy nhất.
- `books` — ISBN duy nhất, `available_quantity >= 0`, FK danh mục, `version`.
- `members` — `member_code` và `email` duy nhất.
- `fine_policies` — mức phí/ngày, trần phí, ngày ân hạn, cờ `is_active`.
- `borrowings` — bạn đọc, ngày mượn, hạn trả, ngày trả, trạng thái, bốn cột tiền phí, `version`.
- `borrowing_details` — một dòng một sách trong phiếu, số lượng và số đã trả.
- `fine_payments` — khoản thu theo phiếu.
- `fine_waivers` — khoản miễn, có lý do và người duyệt.

Index hiện có: phiếu theo bạn đọc + trạng thái, phiếu theo hạn + trạng thái, dòng theo phiếu, thanh toán và miễn giảm theo phiếu.
