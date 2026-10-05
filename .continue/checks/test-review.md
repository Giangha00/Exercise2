---
name: Test Review
description: Code nghiệp vụ mới phải có test tương ứng
---

Fail nếu:

- Có service hoặc nhánh nghiệp vụ mới mà `src/test/java` không có test cho đường đi chính
- Xóa test trong khi code tương ứng vẫn còn
- Đổi cách tính phí hoặc trạng thái phiếu mà không cập nhật `FineCalculationServiceTest` hoặc `BorrowingFlowTest`

Pass nếu chỉ đổi docs, template tĩnh, hoặc test đã cover phần code mới.
