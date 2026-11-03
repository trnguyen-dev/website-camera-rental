-- =====================================================================
-- SCRIPT CƠ SỞ DỮ LIỆU: HỆ THỐNG QUẢN LÝ CHO THUÊ MÁY ẢNH & THIẾT BỊ QUAY PHIM
-- Hệ quản trị CSDL: MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS camera_rental_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE camera_rental_db;

-- 1. Bảng Người dùng (Phân quyền: ADMIN, STAFF)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'ADMIN hoặc STAFF',
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Danh mục thiết bị
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Thiết bị (Máy ảnh, Ống kính, Flycam, Phụ kiện)
CREATE TABLE IF NOT EXISTS equipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    category_id BIGINT NOT NULL,
    daily_rate DECIMAL(12, 2) NOT NULL COMMENT 'Giá thuê / ngày (VNĐ)',
    deposit_amount DECIMAL(12, 2) NOT NULL COMMENT 'Tiền đặt cọc (VNĐ)',
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, RENTED, MAINTENANCE',
    condition_note VARCHAR(255),
    image_url VARCHAR(500),
    CONSTRAINT fk_equipment_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Khách hàng
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    cccd VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    address VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Phiếu thuê (Rental Orders)
CREATE TABLE IF NOT EXISTS rental_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    created_by_id BIGINT,
    start_date DATE NOT NULL,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE,
    rental_days INT NOT NULL,
    total_rental_fee DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_deposit DECIMAL(14, 2) NOT NULL DEFAULT 0,
    late_fee DECIMAL(14, 2) NOT NULL DEFAULT 0,
    damage_fee DECIMAL(14, 2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    final_total_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, RETURNED, CANCELLED',
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_order_user FOREIGN KEY (created_by_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng Chi tiết phiếu thuê (Rental Order Items)
CREATE TABLE IF NOT EXISTS rental_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_order_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    daily_rate DECIMAL(12, 2) NOT NULL,
    deposit_amount DECIMAL(12, 2) NOT NULL,
    sub_total DECIMAL(14, 2) NOT NULL,
    return_condition VARCHAR(255),
    damage_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_item_order FOREIGN KEY (rental_order_id) REFERENCES rental_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_equipment FOREIGN KEY (equipment_id) REFERENCES equipments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- DỮ LIỆU MẪU (SEED DATA)
-- Lưu ý: Spring Boot cũng tự động khởi tạo dữ liệu mẫu này nếu bảng trống
-- =====================================================================
INSERT INTO users (username, password, full_name, role, active) VALUES
('admin', 'admin123', 'Nguyễn Minh Quân (Admin)', 'ADMIN', TRUE),
('nhanvien', 'nv123', 'Lê Thu Hà (Nhân viên)', 'STAFF', TRUE);

INSERT INTO categories (name, description) VALUES
('Body Máy Ảnh', 'Thân máy ảnh Mirrorless, DSLR chuyên nghiệp'),
('Ống Kính (Lens)', 'Ống kính góc rộng, đa dụng, chân dung, tele'),
('Flycam & Gimbal', 'Thiết bị bay quay phim và chống rung'),
('Phụ Kiện & Ánh Sáng', 'Đèn studio, micro thu âm, chân máy, pin sạc');

INSERT INTO equipments (code, name, brand, category_id, daily_rate, deposit_amount, status, condition_note, image_url) VALUES
('CAM-001', 'Sony Alpha A7 Mark IV (Body)', 'Sony', 1, 450000, 5000000, 'AVAILABLE', 'Mới 99%, kèm 2 pin NP-FZ100 + thẻ 64GB', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80'),
('CAM-002', 'Canon EOS R6 Mark II (Body)', 'Canon', 1, 480000, 5000000, 'RENTED', 'Mới 98%, kèm 2 pin LP-E6NH + sạc đôi', 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600&auto=format&fit=crop&q=80'),
('CAM-003', 'Fujifilm X-T5 Silver (Body)', 'Fujifilm', 1, 350000, 3500000, 'AVAILABLE', 'Ngoại hình đẹp, cảm biến 40MP', 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80'),
('LENS-001', 'Sony FE 24-70mm f/2.8 GM II', 'Sony', 2, 300000, 3000000, 'AVAILABLE', 'Kính trong, không mốc xước, kèm filter UV', 'https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=600&auto=format&fit=crop&q=80'),
('LENS-002', 'Canon RF 50mm f/1.2L USM', 'Canon', 2, 280000, 3000000, 'RENTED', 'Lens chân dung cao cấp, kèm hood zin', 'https://images.unsplash.com/photo-1516724562728-afc824a36e84?w=600&auto=format&fit=crop&q=80'),
('LENS-003', 'Sigma 85mm f/1.4 DG DN Art (E-mount)', 'Sigma', 2, 200000, 2000000, 'AVAILABLE', 'Lấy nét nhanh, chuyên chụp chân dung', 'https://images.unsplash.com/photo-1606986628253-05620e9b0a80?w=600&auto=format&fit=crop&q=80'),
('FLY-001', 'DJI Mavic 3 Pro Fly More Combo', 'DJI', 3, 650000, 8000000, 'RENTED', 'Combo 3 pin, tay cầm DJI RC Pro, túi đeo', 'https://images.unsplash.com/photo-1579829366248-204fe8413f31?w=600&auto=format&fit=crop&q=80'),
('GIM-001', 'Gimbal DJI RS3 Pro Combo', 'DJI', 3, 220000, 2500000, 'AVAILABLE', 'Tải trọng 4.5kg, đầy đủ tấm plate + motor focus', 'https://images.unsplash.com/photo-1589872307379-0ffdf9829123?w=600&auto=format&fit=crop&q=80'),
('ACC-001', 'Đèn Studio Aputure Amaran 200x S + Softbox', 'Aputure', 4, 180000, 1500000, 'AVAILABLE', 'Đèn Bi-color 2700K-6500K kèm chân đèn inox', 'https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=600&auto=format&fit=crop&q=80'),
('ACC-002', 'Bộ Micro Thu Âm Rode Wireless PRO (2TX+1RX)', 'Rode', 4, 150000, 1500000, 'MAINTENANCE', 'Đang kiểm tra cổng sạc hộp sạc', 'https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80');

INSERT INTO customers (full_name, phone, cccd, email, address) VALUES
('Phạm Hoàng Nam', '0901234567', '079201001234', 'hoangnam.media@gmail.com', '124 Nguyễn Thị Minh Khai, Quận 3, TP.HCM'),
('Trần Ngọc Ánh', '0912345678', '079202005678', 'ngocanh.studio@gmail.com', '45 Lê Lợi, Quận 1, TP.HCM'),
('Đặng Tuấn Kiệt', '0987654321', '001099004321', 'tuankiet.film@gmail.com', '88 Cầu Giấy, Hà Nội'),
('Vũ Minh Thư', '0938112233', '079203009876', 'minhthu.photo@gmail.com', '210 Võ Văn Ngân, TP. Thủ Đức');


-- ===================================================================
-- 7. BẢNG CÀI ĐẶT HỆ THỐNG (settings) & NHẬT KÝ HOẠT ĐỘNG (activity_logs)
-- ===================================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100);

CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES
('late_fee_percent', '150', 'Hệ số phạt trả trễ mỗi ngày (% giá thuê/ngày)'),
('default_deposit_percent', '1000', 'Tỷ lệ tiền cọc mặc định (% so với đơn giá thuê/ngày)'),
('default_deposit_amount', '3000000', 'Mức tiền cọc cố định mặc định (VNĐ)'),
('max_rental_days', '14', 'Số ngày thuê tối đa cho mỗi phiếu'),
('due_warning_days', '1', 'Số ngày cảnh báo sắp đến hạn trả'),
('shop_name', 'ProCam Rental Studio', 'Tên cửa hàng hiển thị trên hệ thống và phiếu in'),
('shop_address', '128 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 'Địa chỉ cửa hàng trên biên nhận in'),
('shop_phone', '0909 888 999', 'Số điện thoại hotline cửa hàng'),
('page_size', '10', 'Số dòng mặc định hiển thị trên mỗi trang danh sách');

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_username VARCHAR(50) NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
