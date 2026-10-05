# Repo inventory

Gốc mã: `src/main/java/com/example/sem4/exercise2/`.

| Vùng | Nội dung |
|---|---|
| `controller` | `Home`, `Book`, `Member`, `Borrowing`, `FinePolicy`, `Report` |
| `service` | `Book`, `Member`, `Borrowing`, `FinePolicy`, `FineCalculation`, `Report` |
| `repository` | Một repository cho mỗi entity chính |
| `entity` | `Book`, `Category`, `Member`, `Borrowing`, `BorrowingDetail`, `FinePolicy`, `FinePayment`, `FineWaiver` |
| `dto` | Form mượn, trả, thanh toán, miễn giảm, sách, bạn đọc, chính sách |
| `exception` | Hết sách, không đủ điều kiện mượn, trả sai, thanh toán sai, handler UI |
| `config` | `DataInitializer`, `MoneyFormat` |
| `resources/templates` | Màn hình Thymeleaf |
| `resources/db/migration` | `V1__init_schema.sql` |
| `src/test` | `BorrowingFlowTest`, `FineCalculationServiceTest`, context test |

Sinh ra khi build hoặc khi chạy tool, không commit: `target/`, `.codegraph/`, `repomix-output.xml`, `.serena/cache/`, `.serena/logs/`.

Tài liệu cho agent: `.ai/`. Tài liệu cho người: `docs/`. Check PR: `.continue/checks/`.
