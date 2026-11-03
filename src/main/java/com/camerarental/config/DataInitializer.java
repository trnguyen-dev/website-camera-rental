package com.camerarental.config;

import com.camerarental.entity.*;
import com.camerarental.repository.*;
import com.camerarental.service.SettingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerRepository customerRepository;
    private final RentalOrderRepository rentalOrderRepository;
    private final SettingService settingService;
    private final ActivityLogRepository activityLogRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           EquipmentRepository equipmentRepository,
                           CustomerRepository customerRepository,
                           RentalOrderRepository rentalOrderRepository,
                           SettingService settingService,
                           ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerRepository = customerRepository;
        this.rentalOrderRepository = rentalOrderRepository;
        this.settingService = settingService;
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        settingService.ensureDefaultSettings();

        if (userRepository.count() == 0) {
            User admin = userRepository.save(new User(
                    "admin", "admin123", "Nguyễn Minh Quân (Admin)",
                    "0909888999", "admin@procamstudio.vn", User.Role.ADMIN));
            User staff = userRepository.save(new User(
                    "nhanvien", "nv123", "Lê Thu Hà (Nhân viên)",
                    "0912333444", "thuha.staff@procamstudio.vn", User.Role.STAFF));

            Category catBody = categoryRepository.save(new Category("Body Máy Ảnh", "Thân máy ảnh Mirrorless, DSLR chuyên nghiệp"));
            Category catLens = categoryRepository.save(new Category("Ống Kính (Lens)", "Ống kính góc rộng, đa dụng, chân dung, tele"));
            Category catFlycam = categoryRepository.save(new Category("Flycam & Gimbal", "Thiết bị bay quay phim và chống rung"));
            Category catAcc = categoryRepository.save(new Category("Phụ Kiện & Ánh Sáng", "Đèn studio, micro thu âm, chân máy, pin sạc"));

            Equipment eq1 = equipmentRepository.save(new Equipment(
                    "CAM-001", "Sony Alpha A7 Mark IV (Body)", "Sony", catBody,
                    new BigDecimal("450000"), new BigDecimal("5000000"),
                    Equipment.Status.AVAILABLE, "Mới 99%, kèm 2 pin NP-FZ100 + thẻ 64GB",
                    "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80"));

            Equipment eq2 = equipmentRepository.save(new Equipment(
                    "CAM-002", "Canon EOS R6 Mark II (Body)", "Canon", catBody,
                    new BigDecimal("480000"), new BigDecimal("5000000"),
                    Equipment.Status.RENTED, "Mới 98%, kèm 2 pin LP-E6NH + sạc đôi",
                    "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600&auto=format&fit=crop&q=80"));

            Equipment eq3 = equipmentRepository.save(new Equipment(
                    "CAM-003", "Fujifilm X-T5 Silver (Body)", "Fujifilm", catBody,
                    new BigDecimal("350000"), new BigDecimal("3500000"),
                    Equipment.Status.AVAILABLE, "Ngoại hình đẹp, cảm biến 40MP",
                    "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80"));

            Equipment eq4 = equipmentRepository.save(new Equipment(
                    "LENS-001", "Sony FE 24-70mm f/2.8 GM II", "Sony", catLens,
                    new BigDecimal("300000"), new BigDecimal("3000000"),
                    Equipment.Status.AVAILABLE, "Kính trong, không mốc xước, kèm filter UV",
                    "https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=600&auto=format&fit=crop&q=80"));

            Equipment eq5 = equipmentRepository.save(new Equipment(
                    "LENS-002", "Canon RF 50mm f/1.2L USM", "Canon", catLens,
                    new BigDecimal("280000"), new BigDecimal("3000000"),
                    Equipment.Status.RENTED, "Lens chân dung cao cấp, kèm hood zin",
                    "https://images.unsplash.com/photo-1516724562728-afc824a36e84?w=600&auto=format&fit=crop&q=80"));

            Equipment eq6 = equipmentRepository.save(new Equipment(
                    "LENS-003", "Sigma 85mm f/1.4 DG DN Art (E-mount)", "Sigma", catLens,
                    new BigDecimal("200000"), new BigDecimal("2000000"),
                    Equipment.Status.AVAILABLE, "Lấy nét nhanh, chuyên chụp chân dung",
                    "https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=600&auto=format&fit=crop&q=80"));

            Equipment eq7 = equipmentRepository.save(new Equipment(
                    "FLY-001", "DJI Mavic 3 Pro Fly More Combo", "DJI", catFlycam,
                    new BigDecimal("650000"), new BigDecimal("8000000"),
                    Equipment.Status.RENTED, "Combo 3 pin, tay cầm DJI RC Pro, túi đeo",
                    "https://images.unsplash.com/photo-1579829366248-204fe8413f31?w=600&auto=format&fit=crop&q=80"));

            Equipment eq8 = equipmentRepository.save(new Equipment(
                    "GIM-001", "Gimbal DJI RS3 Pro Combo", "DJI", catFlycam,
                    new BigDecimal("220000"), new BigDecimal("2500000"),
                    Equipment.Status.AVAILABLE, "Tải trọng 4.5kg, đầy đủ tấm plate + motor focus",
                    "https://images.unsplash.com/photo-1589872307379-0ffdf9829123?w=600&auto=format&fit=crop&q=80"));

            Equipment eq9 = equipmentRepository.save(new Equipment(
                    "ACC-001", "Đèn Studio Aputure Amaran 200x S + Softbox", "Aputure", catAcc,
                    new BigDecimal("180000"), new BigDecimal("1500000"),
                    Equipment.Status.AVAILABLE, "Đèn Bi-color 2700K-6500K kèm chân đèn inox",
                    "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=600&auto=format&fit=crop&q=80"));

            Equipment eq10 = equipmentRepository.save(new Equipment(
                    "ACC-002", "Bộ Micro Thu Âm Rode Wireless PRO (2TX+1RX)", "Rode", catAcc,
                    new BigDecimal("150000"), new BigDecimal("1500000"),
                    Equipment.Status.MAINTENANCE, "Đang kiểm tra cổng sạc hộp sạc",
                    "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80"));

            Customer cus1 = customerRepository.save(new Customer(
                    "Phạm Hoàng Nam", "0901234567", "079201001234", "hoangnam.media@gmail.com", "124 Nguyễn Thị Minh Khai, Quận 3, TP.HCM"));
            Customer cus2 = customerRepository.save(new Customer(
                    "Trần Ngọc Ánh", "0912345678", "079202005678", "ngocanh.studio@gmail.com", "45 Lê Lợi, Quận 1, TP.HCM"));
            Customer cus3 = customerRepository.save(new Customer(
                    "Đặng Tuấn Kiệt", "0987654321", "001099004321", "tuankiet.film@gmail.com", "88 Cầu Giấy, Hà Nội"));
            Customer cus4 = customerRepository.save(new Customer(
                    "Vũ Minh Thư", "0938112233", "079203009876", "minhthu.photo@gmail.com", "210 Võ Văn Ngân, TP. Thủ Đức"));

            LocalDate today = LocalDate.now();

            RentalOrder order1 = new RentalOrder();
            order1.setOrderCode("PT-001");
            order1.setCustomer(cus1);
            order1.setCreatedBy(admin);
            order1.setStartDate(today.minusMonths(1).withDayOfMonth(10));
            order1.setExpectedReturnDate(today.minusMonths(1).withDayOfMonth(13));
            order1.setActualReturnDate(today.minusMonths(1).withDayOfMonth(13));
            order1.setRentalDays(3);
            order1.setStatus(RentalOrder.Status.RETURNED);
            order1.setNote("Thuê quay MV ca nhạc ngoại cảnh");

            RentalOrderItem item1a = new RentalOrderItem(eq1, eq1.getDailyRate(), eq1.getDepositAmount(), eq1.getDailyRate().multiply(BigDecimal.valueOf(3)));
            item1a.setReturnCondition("Bình thường - Đầy đủ phụ kiện");
            RentalOrderItem item1b = new RentalOrderItem(eq4, eq4.getDailyRate(), eq4.getDepositAmount(), eq4.getDailyRate().multiply(BigDecimal.valueOf(3)));
            item1b.setReturnCondition("Bình thường - Kính sạch");
            order1.addItem(item1a);
            order1.addItem(item1b);

            BigDecimal rentalFee1 = item1a.getSubTotal().add(item1b.getSubTotal());
            BigDecimal deposit1 = item1a.getDepositAmount().add(item1b.getDepositAmount());
            order1.setTotalRentalFee(rentalFee1);
            order1.setTotalDeposit(deposit1);
            order1.setRefundAmount(deposit1);
            order1.setFinalTotalAmount(rentalFee1);
            rentalOrderRepository.save(order1);

            RentalOrder order2 = new RentalOrder();
            order2.setOrderCode("PT-002");
            order2.setCustomer(cus2);
            order2.setCreatedBy(staff);
            order2.setStartDate(today.minusDays(8));
            order2.setExpectedReturnDate(today.minusDays(6));
            order2.setActualReturnDate(today.minusDays(5));
            order2.setRentalDays(2);
            order2.setStatus(RentalOrder.Status.RETURNED);
            order2.setNote("Thuê chụp lookbook thời trang");

            RentalOrderItem item2a = new RentalOrderItem(eq3, eq3.getDailyRate(), eq3.getDepositAmount(), eq3.getDailyRate().multiply(BigDecimal.valueOf(2)));
            item2a.setReturnCondition("Trầy nhẹ nắp cap trước");
            item2a.setDamageFee(new BigDecimal("50000"));
            order2.addItem(item2a);

            BigDecimal rentalFee2 = item2a.getSubTotal();
            BigDecimal deposit2 = item2a.getDepositAmount();
            BigDecimal lateFee2 = new BigDecimal("525000");
            BigDecimal damageFee2 = new BigDecimal("50000");
            order2.setTotalRentalFee(rentalFee2);
            order2.setTotalDeposit(deposit2);
            order2.setLateFee(lateFee2);
            order2.setDamageFee(damageFee2);
            order2.setRefundAmount(deposit2.subtract(lateFee2).subtract(damageFee2));
            order2.setFinalTotalAmount(rentalFee2.add(lateFee2).add(damageFee2));
            rentalOrderRepository.save(order2);

            RentalOrder order3 = new RentalOrder();
            order3.setOrderCode("PT-003");
            order3.setCustomer(cus3);
            order3.setCreatedBy(staff);
            order3.setStartDate(today.minusDays(5));
            order3.setExpectedReturnDate(today.minusDays(2));
            order3.setRentalDays(3);
            order3.setStatus(RentalOrder.Status.ACTIVE);
            order3.setNote("Thuê quay sự kiện tại Đà Lạt (Đang quá hạn 2 ngày)");

            RentalOrderItem item3a = new RentalOrderItem(eq2, eq2.getDailyRate(), eq2.getDepositAmount(), eq2.getDailyRate().multiply(BigDecimal.valueOf(3)));
            RentalOrderItem item3b = new RentalOrderItem(eq5, eq5.getDailyRate(), eq5.getDepositAmount(), eq5.getDailyRate().multiply(BigDecimal.valueOf(3)));
            order3.addItem(item3a);
            order3.addItem(item3b);

            BigDecimal rentalFee3 = item3a.getSubTotal().add(item3b.getSubTotal());
            BigDecimal deposit3 = item3a.getDepositAmount().add(item3b.getDepositAmount());
            order3.setTotalRentalFee(rentalFee3);
            order3.setTotalDeposit(deposit3);
            order3.setRefundAmount(deposit3);
            order3.setFinalTotalAmount(rentalFee3);
            rentalOrderRepository.save(order3);

            RentalOrder order4 = new RentalOrder();
            order4.setOrderCode("PT-004");
            order4.setCustomer(cus4);
            order4.setCreatedBy(admin);
            order4.setStartDate(today);
            order4.setExpectedReturnDate(today.plusDays(3));
            order4.setRentalDays(3);
            order4.setStatus(RentalOrder.Status.ACTIVE);
            order4.setNote("Thuê Flycam quay TVC du lịch biển");

            RentalOrderItem item4a = new RentalOrderItem(eq7, eq7.getDailyRate(), eq7.getDepositAmount(), eq7.getDailyRate().multiply(BigDecimal.valueOf(3)));
            order4.addItem(item4a);

            BigDecimal rentalFee4 = item4a.getSubTotal();
            BigDecimal deposit4 = item4a.getDepositAmount();
            order4.setTotalRentalFee(rentalFee4);
            order4.setTotalDeposit(deposit4);
            order4.setRefundAmount(deposit4);
            order4.setFinalTotalAmount(rentalFee4);
            rentalOrderRepository.save(order4);
        }

        userRepository.findByUsername("admin").ifPresent(u -> {
            if (u.getPhone() == null || u.getPhone().isEmpty()) {
                u.setPhone("0909888999");
                u.setEmail("admin@procamstudio.vn");
                userRepository.save(u);
            }
        });
        userRepository.findByUsername("nhanvien").ifPresent(u -> {
            if (u.getPhone() == null || u.getPhone().isEmpty()) {
                u.setPhone("0912333444");
                u.setEmail("thuha.staff@procamstudio.vn");
                userRepository.save(u);
            }
        });

        if (activityLogRepository.count() == 0) {
            activityLogRepository.save(new ActivityLog("admin", "Nguyễn Minh Quân (Admin)",
                    "KHỞI TẠO HỆ THỐNG", "Khởi tạo cấu hình mặc định cho cửa hàng ProCam Rental Studio"));
            activityLogRepository.save(new ActivityLog("nhanvien", "Lê Thu Hà (Nhân viên)",
                    "LẬP PHIẾU THUÊ", "Lập phiếu thuê PT-003 cho khách hàng Đặng Tuấn Kiệt"));
        }

        // Tự động cập nhật lại link ảnh bị lỗi (LENS-003) trong CSDL nếu đã tạo từ trước
        equipmentRepository.findAll().forEach(eq -> {
            if ("LENS-003".equalsIgnoreCase(eq.getCode())
                    && (eq.getImageUrl() == null || eq.getImageUrl().contains("1606986628253"))) {
                eq.setImageUrl("https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=600&auto=format&fit=crop&q=80");
                equipmentRepository.save(eq);
            }
        });
    }
}
