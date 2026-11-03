# Hệ Thống Quản Lý Cho Thuê Máy Ảnh & Thiết Bị Quay Phim (ProCam Studio)

Dự án Website Quản lý Cho thuê Máy ảnh & Thiết bị Quay phim xây dựng bằng **Spring Boot 3 + Thymeleaf (HTML/CSS/JS) + Spring Data JPA + MySQL**.

---

## 1. Cấu trúc thư mục dự án

- [pom.xml](file:///d:/kỳ%201%20năm%203/phát%20triển%20ứng%20dụng%20web/pom.xml): File cấu hình Maven (Spring Boot, Thymeleaf, JPA, MySQL Connector).
- [database.sql](file:///d:/kỳ%201%20năm%203/phát%20triển%20ứng%20dụng%20web/database.sql): Script khởi tạo CSDL MySQL (`camera_rental_db`), bảng biểu, khóa ngoại và dữ liệu mẫu.
- [application.properties](file:///d:/kỳ%201%20năm%203/phát%20triển%20ứng%20dụng%20web/src/main/resources/application.properties): Cấu hình kết nối MySQL và cổng chạy web (`8080`).
- `src/main/java/com/camerarental/`:
  - `entity/`: Các lớp thực thể (`User`, `Category`, `Equipment`, `Customer`, `RentalOrder`, `RentalOrderItem`).
  - `repository/`: Các interface truy vấn CSDL với Spring Data JPA (`@Query` kiểm tra trùng lịch, lọc đa điều kiện, thống kê).
  - `service/`: Tầng xử lý nghiệp vụ và `@Transactional` (`AuthService`, `EquipmentService`, `CustomerService`, `RentalOrderService`, `ReportService`).
  - `controller/`: Các Controller điều hướng MVC (`AuthController`, `DashboardController`, `EquipmentController`, `CustomerController`, `RentalOrderController`).
  - `config/`: Phân quyền truy cập (`AuthInterceptor`), khởi tạo dữ liệu mẫu tự động (`DataInitializer`).
- `src/main/resources/`:
  - `static/css/style.css`: Giao diện CSS thiết kế hiện đại.
  - `templates/`: Giao diện HTML Thymeleaf (`login.html`, `dashboard.html`, `equipments.html`, `customers.html`, `rental-create.html`, `rentals.html`, `rental-detail.html`, `statistics.html`).

---

## 2. Hướng dẫn chạy dự án trên IntelliJ IDEA

### Bước 1: Bật MySQL
1. Nếu dùng **XAMPP**: Mở **XAMPP Control Panel** -> Bấm **Start** ở dòng **MySQL** (cổng mặc định `3306`, user `root`, password rỗng).
2. Nếu dùng **MySQL Workbench** (có đặt mật khẩu cho tài khoản `root`):
   - Mở file `src/main/resources/application.properties`
   - Điền mật khẩu của bạn vào dòng:
     ```properties
     spring.datasource.password=mat_khau_cua_ban
     ```
   *(Lưu ý: Hệ thống đã bật `createDatabaseIfNotExist=true` và `ddl-auto=update`, đồng thời có `DataInitializer` nên sẽ **tự động tạo database `camera_rental_db`, tự tạo các bảng và tự thêm dữ liệu mẫu** ngay lần chạy đầu tiên mà bạn không cần phải import tay. Ngoài ra file `database.sql` vẫn được cung cấp đầy đủ để bạn nộp kèm báo cáo).*

### Bước 2: Mở và Chạy trong IntelliJ IDEA
1. Mở **IntelliJ IDEA** -> Chọn **Open** -> Trỏ tới thư mục `d:\kỳ 1 năm 3\phát triển ứng dụng web`.
2. Đợi khoảng 15–30 giây ở góc dưới bên phải để IntelliJ tải các thư viện Maven (nếu thấy nút chữ **M** nhỏ của Maven ở góc phải, bấm **Reload All Maven Projects**).
3. Mở file:
   `src/main/java/com/camerarental/CameraRentalApplication.java`
4. Bấm nút **Run (biểu tượng tam giác màu xanh lá)** ở cạnh hàm `main`.
5. Mở trình duyệt truy cập: **http://localhost:8080**

---

## 3. Tài khoản đăng nhập (Phân quyền)

Trên trang đăng nhập có sẵn nút **Bấm để điền nhanh**:
- **Quản trị viên (Admin - Toàn quyền & Xem thống kê):**
  - Username: `admin`
  - Password: `admin123`
- **Nhân viên (Staff - Lập phiếu thuê, Nhận trả, Quản lý khách hàng):**
  - Username: `nhanvien`
  - Password: `nv123`
