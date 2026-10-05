# AI_RULES.md

Quy ước code cho Exercise2.

## Stack

- Java 21, Spring Boot 4.1, đóng gói WAR.
- Web MVC + Thymeleaf, không phải REST API thuần.
- JPA, MySQL (chạy app), H2 (test).
- Flyway: `src/main/resources/db/migration/`.
- Tiền dùng `BigDecimal`. Ngày dùng `LocalDate`.

## Layer

| Package | Việc |
|---|---|
| `controller` | Map URL, form, chuyển hướng, flash message |
| `service` | Nghiệp vụ và transaction |
| `repository` | Spring Data JPA |
| `entity` | Map bảng |
| `dto` | Form và dòng màn hình |
| `exception` | Lỗi nghiệp vụ, `UiExceptionHandler` đưa ra view |
| `config` | Seed dữ liệu, format tiền |

Controller gọi service. Service không trả về HTML.

## Đặt tên

- Entity số ít, bảng số nhiều như schema hiện tại (`books`, `borrowings`).
- URL kebab-case: `/borrowings`, `/fine-policies`, `/reports/overdue`.
- Template Thymeleaf nằm cùng nhóm URL: `templates/borrowings/list.html`.
- Test đặt cạnh hành vi: `src/test/java/.../service/`.

## Dữ liệu

- Số lượng sách không âm. `returned_quantity` không vượt `quantity`.
- Phí tính ở `FineCalculationService`. Chính sách lấy từ `fine_policies` đang active.
- Trạng thái phiếu nằm trong `BorrowingStatus`. Không thêm chuỗi trạng thái tự do ở view.
- Sửa phiếu mượn hoặc tồn kho phải tôn trọng cột `version`.

## Kiểm tra

```bash
./mvnw test
```

App local cần MySQL `library_management` trên `127.0.0.1:3306`, cổng HTTP `8080`.
