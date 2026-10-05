---
name: BA Review
description: Đối chiếu thay đổi với yêu cầu nghiệp vụ
---

Nếu diff không đổi hành vi người dùng nhìn thấy, bỏ qua.

Khi có thay đổi nghiệp vụ, fail nếu:

- Hành vi mới không khớp use case trong `.ai/06_use_cases/` hoặc `docs/BA/`
- Thiếu xử lý trường hợp rỗng, dữ liệu không hợp lệ, hoặc điều kiện không được mượn
- Đổi luồng phiếu mượn, trả, phí mà không cập nhật mô tả

Pass nếu mọi thay đổi hành vi đều có tiêu chí chấp nhận tương ứng.
