# Kế hoạch backend luyện thi TOEIC (`eo-be`)

Backend Spring Boot cho web luyện thi TOEIC Listening & Reading. API giữ cùng đường dẫn với frontend để gắn được màn hình đã có. Không thiết kế schema SQL: MongoDB tạo collection khi ứng dụng chạy.

## Phạm vi

| Hạng mục | Trạng thái |
| --- | --- |
| MongoDB `exam_online`, Mongock seed Role / Skill / Topic Part 1–7 | Xong |
| Entity User, Role, Skill, Topic, Exam, Card, History, Media | Xong |
| Đăng nhập `/app/login`, đăng ký, OTP, đổi / quên / đặt lại mật khẩu | Xong |
| CRUD skill, topic, exam, card và danh sách admin | Xong |
| Thi thử `GET /practice-test` (7 phần, 120 phút) và `POST /exam/collect` | Xong |
| Upload media, phục vụ file tĩnh | Xong |
| Unit test nghiệp vụ cốt lõi (không cần Mongo) | Xong |
| Quy đổi điểm TOEIC 10–990 (`POST /exam/score`) | Xong |
| Ảnh Part 1 và audio Part 1–4 trong `data/` | Xong |

Tài khoản seed sau lần chạy đầu:

- Admin: `admin@toeic.local` / `Admin@123`
- Học viên: `user@toeic.local` / `User@123`

## Cấu trúc đề thi thử

| Part | Tên | Số câu |
| --- | --- | ---: |
| 1 | Photographs | 6 |
| 2 | Question-Response | 25 |
| 3 | Conversations | 39 |
| 4 | Talks | 30 |
| 5 | Incomplete Sentences | 30 |
| 6 | Text Completion | 16 |
| 7 | Reading Comprehension | 54 |
| | Tổng | 200 |

Thời gian: 45 phút nghe + 75 phút đọc = 120 phút. Luyện từng Part vẫn đi theo `skill` → `topic` → `exam`. Thi thử full không dùng `/mini-test`.

## API dùng khi kiểm tra

Mọi phần nội dung dùng cùng một khung. Body luôn bọc trong `responseData`, lỗi trong `error`.

| Hành động | Skill | Topic | Exam | Card |
| --- | --- | --- | --- | --- |
| Tạo | `POST /skill` | `POST /topic` | `POST /exam` | `POST /card` |
| Sửa | `PUT /skill` | `PUT /topic` | `PUT /exam` | `PUT /card` |
| Xóa nhiều | `PUT /skill/delete/multiple` | `PUT /topic/delete/multiple` | `PUT /exam/delete/multiple` | `PUT /card/delete/multiple` |
| Chi tiết | `GET /skill/{id}` | `GET /topic/{id}` | `GET /exam/{id}` | `GET /card/{id}` |
| Danh sách admin | `GET /admin/skill` | `GET /admin/topic` | `GET /admin/exam` | `GET /admin/card` |
| Gắn phần con | `POST /skill/add-topic` `{id, topicIds}` | `POST /topic/add-exam` `{id, examIds}` | `POST /exam/add-card` `{id, cardIds}` | `POST /card/import` `{filePath}` |

Request tạo/sửa cùng tên field với response: `name`, danh sách id con (`topicIds`, `examIds`, `cardIds`). Xóa nhiều luôn gửi `{ "ids": [] }` và trả `true`.

| Method | Path | Ý nghĩa |
| --- | --- | --- |
| POST | `/app/login` | Đăng nhập, trả JWT. Tài khoản còn OTP thì bị từ chối |
| POST | `/public/register/user` | Đăng ký, luôn gán `ROLE_USER` |
| GET | `/skill/header` | Skill kèm topic cho trang chủ |
| GET | `/topic/exam/{topicId}` | Danh sách bài trong một part |
| GET | `/exam/card/{examId}` | Câu hỏi của một bài luyện |
| GET | `/practice-test` | Đề thi thử, cần JWT |
| POST | `/exam/score` | Quy đổi số câu đúng Listening/Reading sang điểm 5–495, tổng 10–990 |
| POST | `/exam/collect` | Lưu điểm thi thử vào History của user đang đăng nhập |
| POST | `/user/pwd/change` | Đổi mật khẩu, kiểm tra mật khẩu cũ và xác nhận |
| POST | `/user/pwd/forgot`, `/user/pwd/reset` | Quên mật khẩu bằng mã reset |
| POST | `/admin/users/block`, `/admin/users/unblock` | Khóa / mở tài khoản, chỉ admin |

Collection Postman nằm ở `postman/`.

## Unit test

Test nằm ở `src/test/java`, dùng JUnit 5 và Mockito. Không bật Spring context và không cần MongoDB.

| Lớp test | Việc được kiểm |
| --- | --- |
| `AuthServiceImplTest` | Login thành công, từ chối khi còn OTP, sai mật khẩu, logout |
| `UserServiceImplTest` | Chuẩn hóa email, mã hóa mật khẩu, email trùng, đổi / reset mật khẩu, khóa user |
| `RegistrationServiceImplTest` | Đăng ký luôn là học viên |
| `ExamServiceImplTest` | Thời lượng thi thử, đếm câu, gộp Part 7, thứ tự card, xóa mềm, gọi bảng điểm |
| `ToeicScoreTest` | 0 câu = 10 điểm, 200 câu = 990, mốc giữa bảng, điểm tăng dần |
| `HistoryServiceImplTest` | Tạo lần làm bài, cập nhật progress, bắt buộc đăng nhập |
| `NotificationServiceImplTest` | OTP 6 số, xác thực và xóa OTP |
| `ExamUserDetailsServiceTest` | Tìm user, email rỗng, role rỗng |
| `JwtUtilsTest` | Ký và đọc JWT, token hỏng hoặc hết hạn |
| `HistoryRequestTest` | Progress nhận số, chuỗi hoặc null |

Chạy:

```
.\gradlew.bat test
```

CI (`.github/workflows/ci.yml`) chạy cùng lệnh `gradle test` trên mỗi push và pull request.

## Điểm và media

`POST /exam/score` nhận `listeningCorrect` và `readingCorrect` (0–100). Mỗi phần ra điểm 5–495, bước 5. Không làm bài là 10, đúng hết là 990. Frontend thi thử dùng cùng bảng và hiện `Listening x/100`, `Reading x/100`.

Ảnh Part 1 và file nghe Part 1–4 nằm ở `data/images`, `data/sounds`. Script tạo lại: `scripts/generate-practice-media.ps1`. Câu chữ trong đề vẫn là nội dung luyện tập, không phải đề ETS chính thức.

Màn quên / đặt lại mật khẩu của frontend đã gọi `/user/pwd/forgot` và `/user/pwd/reset`.
