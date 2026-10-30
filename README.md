# Chicken Shop – Website đặt gà rán online

Dự án cuối môn Spring Boot. Khách xem thực đơn, thêm giỏ hàng (AJAX), đặt hàng thanh toán khi nhận; admin quản lý món, danh mục, đơn hàng và xem dashboard thống kê.

## Công nghệ
Spring Boot 4.1.1 · Gradle · Thymeleaf · Spring Data JPA (Hibernate) · MySQL · Spring Security (BCrypt, CSRF) · Bean Validation · i18n Việt/Anh · AJAX (fetch) · Actuator · JUnit 5, Mockito

## Yêu cầu
- JDK 21 (sửa lại nếu bạn dùng bản khác, xem `build.gradle`)
- MySQL 8

## Cách chạy
1. Tạo database:
   ```sql
   CREATE DATABASE chickenshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Sao chép `src/main/resources/application.yaml.example` thành `src/main/resources/application.yaml`, điền username và password MySQL.
3. Chạy:
   ```
   .\gradlew.bat bootRun
   ```
4. Mở http://localhost:8080

## Tài khoản demo (tự tạo ở lần chạy đầu)
| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị | admin | Admin@123 |
| Khách hàng | customer1 | Customer@123 |

## Chức năng
- Khách: thực đơn (lọc, tìm, phân trang), đăng ký, đăng nhập, giỏ hàng AJAX, đặt hàng, lịch sử và hủy đơn, đổi ngôn ngữ VN | EN
- Admin: quản lý món (upload ảnh, ẩn hiện), danh mục, đơn hàng (đổi trạng thái đúng thứ tự)
- Dashboard `/admin/dashboard`: doanh thu, đơn hàng, giờ cao điểm, cơ cấu danh mục, thanh toán, top món, top khách, tồn kho thấp

## Đóng gói và chạy bằng jar
```
.\gradlew.bat bootJar
java -jar build\libs\<tên-file>.jar
```
Chạy từ thư mục gốc dự án để thư mục `uploads/` nằm cạnh.

## Kiểm tra sức khỏe
`GET /actuator/health` trả `{"status":"UP"}`

## Chạy test
```
.\gradlew.bat test
```

## Cấu trúc
`model` · `repository` · `service` · `controller` · `dto` · `config` · `exception`