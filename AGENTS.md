# AGENTS.md

Quy tắc làm việc cho mọi AI agent trên repo Exercise2 (quản lý mượn sách).

## Đọc trước khi sửa code

Đọc theo thứ tự, dừng khi đã đủ ngữ cảnh cho việc đang làm:

1. `AI_RULES.md`
2. `.ai/00_project_brief.md`
3. `.ai/02_architecture_map.md`
4. File đúng vai trong `.ai/prompts/`
5. Tài liệu module hoặc use case liên quan trong `.ai/03_module_summaries/` và `.ai/06_use_cases/`

Không đọc cả repo. Với Java, ưu tiên Serena (`find_symbol`, `find_referencing_symbols`, `get_symbols_overview`) trước khi mở full file.

## Được làm

- Sửa code trong `src/` theo đúng layer: controller chỉ điều phối, nghiệp vụ nằm ở `service`.
- Thêm migration Flyway mới trong `src/main/resources/db/migration/`. Không sửa `V1__init_schema.sql` sau khi đã chạy.
- Thêm hoặc sửa test trong `src/test/` cho hành vi nghiệp vụ vừa đổi.
- Viết tài liệu ổn định vào `docs/` và `.ai/`.
- Ghi kết quả tạm của một lượt agent vào `.ai/outputs/`.

## Không được làm

- Không commit `.codegraph/`, `target/`, `.idea/`, `repomix-output.xml`, `.serena/cache/`, `.serena/logs/`.
- Không đổi schema bằng `ddl-auto`. Hibernate đang `validate`. Schema chỉ đi qua Flyway.
- Không nhét nghiệp vụ mượn, trả, phí vào controller hoặc template Thymeleaf.
- Không bỏ qua tồn kho, optimistic lock (`version`), hoặc trạng thái phiếu mượn khi sửa `BorrowingService`.
- Không đưa mật khẩu, connection string thật, hoặc dữ liệu cá nhân vào git. Cấu hình local giữ trong `application.properties` của máy dev; không commit secret mới.
- Không coi file trong `.ai/outputs/` là nguồn sự thật. Nguồn sự thật là code, migration, và `docs/`.

## Format output

Khi kết thúc một nhiệm vụ, ghi một file markdown trong `.ai/outputs/` tên `YYYY-MM-DD-<vai>-<chu-de>.md` với các mục:

- Việc đã làm
- File đã đụng
- Cách kiểm tra
- Rủi ro còn lại

Trong chat, trả lời ngắn: kết quả, file chính, lệnh test đã chạy hoặc cần chạy.

## Tiêu chuẩn PR

Trước khi mở PR, chạy mọi check trong `.continue/checks/` trên diff và sửa các mục fail.

Mỗi PR cần:

- Mô tả hành vi người dùng thay đổi và tiêu chí chấp nhận.
- Test cho nhánh nghiệp vụ mới, hoặc ghi rõ vì sao không có test.
- Migration mới nếu đổi bảng. Không sửa migration đã apply.
- Không kèm file sinh ra (`target/`, `.codegraph/`, `repomix-output.xml`).
