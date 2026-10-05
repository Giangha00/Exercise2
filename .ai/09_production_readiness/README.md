# Production readiness

Repo này là bài tập chạy local. Chưa sẵn sàng production.

Trước khi triển khai thật, xử lý từng mục và ghi quyết định vào `docs/ADR/`:

- Mật khẩu MySQL không để trống và không commit.
- `show-sql` tắt. `thymeleaf.cache` bật.
- `open-in-view` xem lại.
- WAR trên Tomcat, cổng và profile tách khỏi `application.properties` dev.
- Backup MySQL và thứ tự migration Flyway.
- Không phơi stack trace ra `templates/error.html`.
- Log không ghi email, điện thoại, hoặc số tiền của từng bạn đọc ở mức debug trên môi trường chung.
