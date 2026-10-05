# API inventory

Ứng dụng là server-rendered MVC. "API" ở đây là các URL form, không phải REST JSON.

| Method | Path | Việc |
|---|---|---|
| GET | `/` | Trang chủ |
| GET | `/books` | Danh sách sách |
| GET | `/books/new` | Form sách |
| POST | `/books` | Lưu sách |
| POST | `/categories` | Lưu danh mục |
| GET | `/members` | Danh sách bạn đọc |
| GET | `/members/new` | Form bạn đọc |
| POST | `/members` | Lưu bạn đọc |
| GET | `/borrowings` | Danh sách phiếu |
| GET | `/borrowings/new` | Form mượn |
| POST | `/borrowings` | Tạo phiếu |
| GET | `/borrowings/{id}` | Chi tiết |
| POST | `/borrowings/{id}/cancel` | Hủy phiếu |
| GET | `/borrowings/{id}/return` | Form trả |
| POST | `/borrowings/{id}/return` | Ghi nhận trả |
| GET | `/borrowings/{id}/payments/new` | Form thanh toán |
| POST | `/borrowings/{id}/payments` | Ghi nhận thanh toán |
| POST | `/borrowings/{id}/waivers` | Ghi nhận miễn giảm |
| GET | `/fine-policies` | Danh sách chính sách |
| POST | `/fine-policies` | Lưu chính sách |
| GET | `/reports/overdue` | Báo cáo quá hạn |

Khi thêm URL, cập nhật bảng này và template tương ứng.
