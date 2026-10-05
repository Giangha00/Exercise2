---
name: Style Review
description: Giữ layer và quy ước của repo
---

Chỉ xét code vừa thêm hoặc sửa. Bỏ qua format thuần; phần đó để compiler và formatter.

Fail nếu:

- Nghiệp vụ mượn, trả, hoặc phí nằm trong controller hoặc template
- Schema đổi bằng cách sửa entity mà không có migration Flyway mới, hoặc sửa `V1__init_schema.sql`
- Tên URL, template, hoặc class lệch quy ước trong `AI_RULES.md`
- Tiền dùng `double` thay vì `BigDecimal`

Pass nếu code mới đi đúng layer hiện có.
