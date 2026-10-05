# Test matrix

Lệnh: `./mvnw test`. Profile test dùng H2, không cần MySQL.

| Hành vi | Test hiện có | Ghi chú |
|---|---|---|
| Tính phí theo ngày, ân hạn, trần | `FineCalculationServiceTest` | Service thuần |
| Mượn, trả, hủy, thanh toán, miễn, quá hạn, tồn kho đồng thời | `BorrowingFlowTest` | `@SpringBootTest` |
| Context khởi động | `Exercise2ApplicationTests` | Không kiểm nghiệp vụ |
| Màn hình Thymeleaf | Chưa có | Thêm khi đổi form |
| Flyway trên MySQL | Chưa có | Chỉ kiểm khi đổi migration |

Khi thêm nhánh nghiệp vụ, thêm một hàng vào bảng này cùng test tương ứng.
