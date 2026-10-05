# Use cases

Mỗi use case một file: tác nhân, tiền điều kiện, luồng chính, luồng lỗi, dữ liệu đổi.

| File gợi ý | Luồng |
|---|---|
| `muon-sach.md` | Tạo phiếu, trừ `available_quantity`, từ chối khi hết sách hoặc bạn đọc không đủ điều kiện |
| `tra-sach.md` | Trả một phần hoặc hết, tính ngày trễ và phí |
| `huy-phieu.md` | Hủy phiếu đang mượn, hoàn tồn |
| `thanh-toan-phi.md` | Thu không vượt số còn nợ |
| `mien-giam.md` | Giảm phí có lý do và người duyệt |
| `chinh-sach-phi.md` | Một chính sách active dùng cho lần tính tiếp theo |
| `bao-cao-qua-han.md` | Liệt kê phiếu quá hạn |

Ngoại lệ đã có trong code: `InsufficientStockException`, `MemberNotEligibleException`, `InvalidReturnException`, `InvalidPaymentException`.
