# eo-be

Backend luyện thi TOEIC (Listening & Reading): Spring Boot 2.6, Java 11, MongoDB, JWT.

Kế hoạch module, API và phạm vi unit test nằm ở [docs/PLAN.md](docs/PLAN.md).

## Chạy local

Cần MongoDB tại `localhost:27017`. Có thể dùng Docker:

```
docker compose -f docker-compose.yml -f docker-compose.dev-mongo.yml up mongo -d
.\gradlew.bat bootRun
```

Cổng 27017 chỉ mở cho `127.0.0.1` nhờ `docker-compose.dev-mongo.yml`. Chạy cả stack để demo bằng `docker compose up --build` rồi mở `http://localhost:3000`; lúc này BE và Mongo chỉ nằm trong mạng Docker.

Ứng dụng lắng nghe cổng `8080`. Kiểm tra trạng thái ở `http://localhost:8080/actuator/health`.

Lần chạy đầu, Mongock tạo database `exam_online` và seed role, skill, topic Part 1–7, admin và user mẫu. Tài khoản ghi trong `docs/PLAN.md`. Ảnh và audio luyện nghe tạo bằng `scripts/generate-practice-media.ps1` nếu thư mục `data/images` hoặc `data/sounds` còn trống.

## Kiểm tra

```
.\gradlew.bat test
```

Unit test không cần MongoDB. Collection Postman nằm ở `postman/`.
