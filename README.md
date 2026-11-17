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

## Sao lưu trên VPS

`deploy/deploy.sh` cài lịch chạy `deploy/backup.sh` lúc 02:30 mỗi ngày. Mỗi lần chạy lưu vào `/opt/toeic/backups`:

- `mongo-<thời điểm>.archive.gz`: toàn bộ database `exam_online` (mongodump).
- `data-<thời điểm>.tar.gz`: ảnh và file nghe trong `/opt/toeic/data`.

Chỉ giữ 7 bản mới nhất mỗi loại. Nhật ký ở `/opt/toeic/backups/backup.log`. Sao lưu ngay bằng tay:

```bash
/opt/toeic/eo-be/deploy/backup.sh
```

Khôi phục (ghi đè dữ liệu hiện tại):

```bash
cd /opt/toeic/eo-be/deploy
docker compose -f docker-compose.prod.yml exec -T mongo mongorestore --archive --gzip --drop < /opt/toeic/backups/mongo-<thời điểm>.archive.gz
tar -xzf /opt/toeic/backups/data-<thời điểm>.tar.gz -C /opt/toeic
```

Bản sao lưu nằm cùng VPS, nên nếu VPS hết hạn hay bị xóa thì mất cả bản sao lưu. Thỉnh thoảng chép thư mục `/opt/toeic/backups` về máy mình.
