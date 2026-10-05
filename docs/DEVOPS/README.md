# DevOps

Cách chạy local và những việc còn thiếu trước khi đưa ra khỏi máy dev. Xem `.ai/09_production_readiness/`.

- MySQL trên `127.0.0.1:3306`, database `library_management` (Flyway tạo nếu chưa có).
- `./mvnw spring-boot:run`
- Test: `./mvnw test` (H2, không cần MySQL).
