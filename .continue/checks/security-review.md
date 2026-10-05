---
name: Security Review
description: Bắt secret, injection, và thiếu kiểm tra đầu vào
---

Fail nếu thấy trong phần code vừa đổi:

- Mật khẩu, API key, token, hoặc connection string mới bị commit
- Input form không được kiểm tra trước khi ghi database
- Câu query ghép bằng chuỗi thay vì tham số
- Email, điện thoại, hoặc số tiền bạn đọc bị ghi ra log

Pass nếu không có mục nào ở trên.
