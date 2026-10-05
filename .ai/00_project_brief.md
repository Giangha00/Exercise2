# Project brief

Exercise2 là ứng dụng quản lý thư viện cho môn học: sách, bạn đọc, phiếu mượn, trả sách, phí trễ hạn, miễn giảm và báo cáo quá hạn.

Người dùng thao tác qua trình duyệt. Server render Thymeleaf. Không có tài khoản đăng nhập trong phạm vi hiện tại.

## Việc hệ thống làm

- Danh mục và sách, theo dõi số lượng còn.
- Bạn đọc (mã, tên, email, điện thoại).
- Tạo phiếu mượn nhiều đầu sách, hủy phiếu, trả một phần hoặc toàn bộ.
- Tính phí theo chính sách đang bật (mức/ngày, ân hạn, trần phí).
- Ghi nhận thanh toán và miễn giảm.
- Báo cáo phiếu quá hạn.

## Ngoài phạm vi hiện tại

- Đăng nhập, phân quyền thủ thư và bạn đọc.
- API JSON công khai.
- Triển khai production (xem `.ai/09_production_readiness/`).
