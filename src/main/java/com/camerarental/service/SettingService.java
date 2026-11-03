package com.camerarental.service;

import com.camerarental.entity.*;
import com.camerarental.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SettingService {

    public static final String KEY_LATE_FEE_PERCENT = "late_fee_percent";
    public static final String KEY_DEFAULT_DEPOSIT_PERCENT = "default_deposit_percent";
    public static final String KEY_DEFAULT_DEPOSIT_AMOUNT = "default_deposit_amount";
    public static final String KEY_MAX_RENTAL_DAYS = "max_rental_days";
    public static final String KEY_DUE_WARNING_DAYS = "due_warning_days";
    public static final String KEY_SHOP_NAME = "shop_name";
    public static final String KEY_SHOP_ADDRESS = "shop_address";
    public static final String KEY_SHOP_PHONE = "shop_phone";
    public static final String KEY_PAGE_SIZE = "page_size";

    private final SystemSettingRepository settingRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final CategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerRepository customerRepository;
    private final RentalOrderRepository rentalOrderRepository;

    public SettingService(SystemSettingRepository settingRepository,
                          UserRepository userRepository,
                          ActivityLogRepository activityLogRepository,
                          CategoryRepository categoryRepository,
                          EquipmentRepository equipmentRepository,
                          CustomerRepository customerRepository,
                          RentalOrderRepository rentalOrderRepository) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerRepository = customerRepository;
        this.rentalOrderRepository = rentalOrderRepository;
    }

    @Transactional
    public void ensureDefaultSettings() {
        saveDefaultIfAbsent(KEY_LATE_FEE_PERCENT, "150", "Hệ số phạt trả trễ mỗi ngày (% giá thuê/ngày)");
        saveDefaultIfAbsent(KEY_DEFAULT_DEPOSIT_PERCENT, "1000", "Tỷ lệ tiền cọc mặc định (% so với đơn giá thuê/ngày)");
        saveDefaultIfAbsent(KEY_DEFAULT_DEPOSIT_AMOUNT, "3000000", "Mức tiền cọc cố định mặc định (VNĐ)");
        saveDefaultIfAbsent(KEY_MAX_RENTAL_DAYS, "14", "Số ngày thuê tối đa cho mỗi phiếu");
        saveDefaultIfAbsent(KEY_DUE_WARNING_DAYS, "1", "Số ngày cảnh báo sắp đến hạn trả");
        saveDefaultIfAbsent(KEY_SHOP_NAME, "ProCam Rental Studio", "Tên cửa hàng hiển thị trên hệ thống và phiếu in");
        saveDefaultIfAbsent(KEY_SHOP_ADDRESS, "128 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", "Địa chỉ cửa hàng trên biên nhận in");
        saveDefaultIfAbsent(KEY_SHOP_PHONE, "0909 888 999", "Số điện thoại hotline cửa hàng");
        saveDefaultIfAbsent(KEY_PAGE_SIZE, "10", "Số dòng mặc định hiển thị trên mỗi trang danh sách");
    }

    private void saveDefaultIfAbsent(String key, String defaultValue, String description) {
        if (!settingRepository.existsById(key)) {
            settingRepository.save(new SystemSetting(key, defaultValue, description));
        }
    }

    public Map<String, String> getAllSettingsMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY_LATE_FEE_PERCENT, "150");
        map.put(KEY_DEFAULT_DEPOSIT_PERCENT, "1000");
        map.put(KEY_DEFAULT_DEPOSIT_AMOUNT, "3000000");
        map.put(KEY_MAX_RENTAL_DAYS, "14");
        map.put(KEY_DUE_WARNING_DAYS, "1");
        map.put(KEY_SHOP_NAME, "ProCam Rental Studio");
        map.put(KEY_SHOP_ADDRESS, "128 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
        map.put(KEY_SHOP_PHONE, "0909 888 999");
        map.put(KEY_PAGE_SIZE, "10");

        for (SystemSetting s : settingRepository.findAll()) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    public String getString(String key, String defaultValue) {
        return settingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(defaultValue)).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Transactional
    public void updateSystemSettings(Map<String, String> updates, User actor) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue() != null ? entry.getValue().trim() : "";
            SystemSetting setting = settingRepository.findById(key)
                    .orElse(new SystemSetting(key, val, ""));
            setting.setSettingValue(val);
            settingRepository.save(setting);
        }
        logActivity(actor, "CẬP NHẬT CÀI ĐẶT HỆ THỐNG",
                "Đã lưu cấu hình tham số nghiệp vụ & thông tin cửa hàng (" + updates.getOrDefault(KEY_SHOP_NAME, "") + ")");
    }

    @Transactional
    public User updateOwnProfile(Long userId, String fullName, String phone, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }
        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        user.setEmail(email != null ? email.trim() : null);
        User saved = userRepository.save(user);
        logActivity(saved, "CẬP NHẬT HỒ SƠ CÁ NHÂN", "Cập nhật thông tin cá nhân: " + saved.getFullName());
        return saved;
    }

    @Transactional
    public void changeOwnPassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));
        if (oldPassword == null || !user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác!");
        }
        if (newPassword == null || newPassword.trim().length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự!");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp!");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
        logActivity(user, "ĐỔI MẬT KHẨU", "Đã thay đổi mật khẩu tài khoản cá nhân");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String username, String password, String fullName,
                           String phone, String email, User.Role role, User adminActor) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        String cleanUsername = username.trim().toLowerCase();
        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập '" + cleanUsername + "' đã tồn tại!");
        }
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 4 ký tự!");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên nhân viên không được để trống!");
        }
        User newUser = new User(
                cleanUsername,
                password.trim(),
                fullName.trim(),
                phone != null ? phone.trim() : null,
                email != null ? email.trim() : null,
                role != null ? role : User.Role.STAFF
        );
        User saved = userRepository.save(newUser);
        logActivity(adminActor, "THÊM TÀI KHOẢN MỚI",
                "Tạo tài khoản @" + saved.getUsername() + " (" + saved.getFullName() + " - " + saved.getRole().name() + ")");
        return saved;
    }

    @Transactional
    public void changeUserRole(Long targetUserId, User.Role newRole, User adminActor) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        if (target.getId().equals(adminActor.getId()) && newRole != User.Role.ADMIN) {
            throw new IllegalArgumentException("Bạn không thể tự hạ quyền Quản trị viên của chính mình!");
        }
        target.setRole(newRole);
        userRepository.save(target);
        logActivity(adminActor, "PHÂN QUYỀN TÀI KHOẢN",
                "Đổi vai trò tài khoản @" + target.getUsername() + " thành " + newRole.getDisplayName());
    }

    @Transactional
    public void toggleUserActive(Long targetUserId, User adminActor) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        if (target.getId().equals(adminActor.getId())) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản đang đăng nhập của chính mình!");
        }
        target.setActive(!target.isActive());
        userRepository.save(target);
        logActivity(adminActor, target.isActive() ? "MỞ KHÓA TÀI KHOẢN" : "KHÓA TÀI KHOẢN",
                (target.isActive() ? "Mở khóa" : "Khóa") + " tài khoản @" + target.getUsername() + " (" + target.getFullName() + ")");
    }

    @Transactional
    public void resetUserPassword(Long targetUserId, String newPassword, User adminActor) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        if (newPassword == null || newPassword.trim().length() < 4) {
            throw new IllegalArgumentException("Mật khẩu đặt lại phải có ít nhất 4 ký tự!");
        }
        target.setPassword(newPassword.trim());
        userRepository.save(target);
        logActivity(adminActor, "ĐẶT LẠI MẬT KHẨU",
                "Đã đặt lại mật khẩu mới cho tài khoản @" + target.getUsername() + " (" + target.getFullName() + ")");
    }

    @Transactional
    public void logActivity(User actor, String actionType, String details) {
        String username = (actor != null) ? actor.getUsername() : "system";
        String fullName = (actor != null) ? actor.getFullName() : "Hệ thống";
        activityLogRepository.save(new ActivityLog(username, fullName, actionType, details));
    }

    public List<ActivityLog> getRecentLogs() {
        return activityLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public String generateSqlBackup(User actor) {
        logActivity(actor, "SAO LƯU DỮ LIỆU", "Xuất file sao lưu cơ sở dữ liệu (.sql)");
        StringBuilder sb = new StringBuilder();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        sb.append("-- ===================================================================\n");
        sb.append("-- BẢN SAO LƯU DỮ LIỆU HỆ THỐNG PROCAM STUDIO\n");
        sb.append("-- Thời gian sao lưu: ").append(now).append("\n");
        sb.append("-- Người thực hiện: ").append(actor != null ? actor.getFullName() : "Admin").append("\n");
        sb.append("-- ===================================================================\n\n");

        sb.append("-- 1. BẢNG CÀI ĐẶT HỆ THỐNG (settings)\n");
        for (SystemSetting s : settingRepository.findAll()) {
            sb.append(String.format(
                    "REPLACE INTO settings (setting_key, setting_value, description) VALUES ('%s', '%s', '%s');\n",
                    escapeSql(s.getSettingKey()), escapeSql(s.getSettingValue()), escapeSql(s.getDescription())));
        }

        sb.append("\n-- 2. BẢNG NGƯỜI DÙNG (users)\n");
        for (User u : userRepository.findAll()) {
            sb.append(String.format(
                    "REPLACE INTO users (id, username, password, full_name, phone, email, role, active) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %d);\n",
                    u.getId(), escapeSql(u.getUsername()), escapeSql(u.getPassword()),
                    escapeSql(u.getFullName()), escapeSql(u.getPhone()), escapeSql(u.getEmail()),
                    u.getRole().name(), u.isActive() ? 1 : 0));
        }

        sb.append("\n-- 3. BẢNG DANH MỤC (categories)\n");
        for (Category c : categoryRepository.findAll()) {
            sb.append(String.format(
                    "REPLACE INTO categories (id, name, description) VALUES (%d, '%s', '%s');\n",
                    c.getId(), escapeSql(c.getName()), escapeSql(c.getDescription())));
        }

        sb.append("\n-- 4. BẢNG THIẾT BỊ (equipments)\n");
        for (Equipment e : equipmentRepository.findAll()) {
            sb.append(String.format(
                    "REPLACE INTO equipments (id, code, name, brand, category_id, daily_rate, deposit_amount, status, condition_note) VALUES (%d, '%s', '%s', '%s', %d, %s, %s, '%s', '%s');\n",
                    e.getId(), escapeSql(e.getCode()), escapeSql(e.getName()), escapeSql(e.getBrand()),
                    e.getCategory() != null ? e.getCategory().getId() : 1L,
                    e.getDailyRate().toPlainString(), e.getDepositAmount().toPlainString(),
                    e.getStatus().name(), escapeSql(e.getConditionNote())));
        }

        sb.append("\n-- 5. BẢNG KHÁCH HÀNG (customers)\n");
        for (Customer c : customerRepository.findAll()) {
            sb.append(String.format(
                    "REPLACE INTO customers (id, full_name, phone, cccd, email, address) VALUES (%d, '%s', '%s', '%s', '%s', '%s');\n",
                    c.getId(), escapeSql(c.getFullName()), escapeSql(c.getPhone()),
                    escapeSql(c.getCccd()), escapeSql(c.getEmail()), escapeSql(c.getAddress())));
        }

        sb.append("\n-- 6. THỐNG KÊ TỔNG QUAN PHIẾU THUÊ (rental_orders: ").append(rentalOrderRepository.count()).append(" phiếu)\n");
        for (RentalOrder o : rentalOrderRepository.findAll()) {
            sb.append(String.format(
                    "-- Phiếu %s | Khách: %s | Từ %s đến %s | Trạng thái: %s | Doanh thu: %s VNĐ\n",
                    o.getOrderCode(), o.getCustomer().getFullName(),
                    o.getStartDate(), o.getExpectedReturnDate(),
                    o.getStatus().name(), o.getFinalTotalAmount().toPlainString()));
        }

        return sb.toString();
    }

    private String escapeSql(String val) {
        if (val == null) return "";
        return val.replace("'", "''");
    }
}
