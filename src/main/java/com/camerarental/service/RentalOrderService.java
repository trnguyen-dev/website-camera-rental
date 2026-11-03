package com.camerarental.service;

import com.camerarental.dto.CreateOrderRequest;
import com.camerarental.dto.ReturnOrderRequest;
import com.camerarental.entity.*;
import com.camerarental.repository.CustomerRepository;
import com.camerarental.repository.EquipmentRepository;
import com.camerarental.repository.RentalOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RentalOrderService {

    private final RentalOrderRepository rentalOrderRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerRepository customerRepository;
    private final SettingService settingService;

    public RentalOrderService(RentalOrderRepository rentalOrderRepository,
                              EquipmentRepository equipmentRepository,
                              CustomerRepository customerRepository,
                              SettingService settingService) {
        this.rentalOrderRepository = rentalOrderRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerRepository = customerRepository;
        this.settingService = settingService;
    }

    public List<RentalOrder> searchOrders(String keyword, RentalOrder.Status status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return rentalOrderRepository.searchOrders(cleanKeyword, status);
    }

    public RentalOrder getOrderById(Long id) {
        return rentalOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu thuê với ID: " + id));
    }

    public int calculateRentalDays(LocalDate startDate, LocalDate expectedReturnDate) {
        if (startDate == null || expectedReturnDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ ngày bắt đầu và ngày hẹn trả!");
        }
        if (expectedReturnDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày hẹn trả không được nhỏ hơn ngày bắt đầu thuê!");
        }
        long diff = ChronoUnit.DAYS.between(startDate, expectedReturnDate);
        int days = (int) Math.max(1L, diff);

        int maxRentalDays = settingService.getInt(SettingService.KEY_MAX_RENTAL_DAYS, 14);
        if (maxRentalDays > 0 && days > maxRentalDays) {
            throw new IllegalArgumentException("Số ngày thuê (" + days + " ngày) vượt quá giới hạn tối đa cho phép trong Cài đặt hệ thống (" + maxRentalDays + " ngày)!");
        }
        return days;
    }

    public Map<Long, String> getEquipmentConflicts(LocalDate startDate, LocalDate expectedReturnDate) {
        Map<Long, String> conflicts = new HashMap<>();
        if (startDate == null || expectedReturnDate == null || expectedReturnDate.isBefore(startDate)) {
            return conflicts;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Equipment> allEquipments = equipmentRepository.findAll();
        for (Equipment eq : allEquipments) {
            if (eq.getStatus() == Equipment.Status.MAINTENANCE) {
                conflicts.put(eq.getId(), "Đang bảo trì / hỏng");
                continue;
            }
            List<RentalOrder> conflictingOrders = rentalOrderRepository
                    .findConflictingOrdersForEquipment(eq.getId(), startDate, expectedReturnDate);
            if (!conflictingOrders.isEmpty()) {
                RentalOrder first = conflictingOrders.get(0);
                conflicts.put(eq.getId(),
                        "Trùng phiếu " + first.getOrderCode() + " (" +
                        first.getStartDate().format(fmt) + " - " +
                        first.getExpectedReturnDate().format(fmt) + ")");
            }
        }
        return conflicts;
    }

    @Transactional(rollbackFor = Exception.class)
    public RentalOrder createRentalOrder(CreateOrderRequest request, User currentUser) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng thuê thiết bị!");
        }
        if (request.getEquipmentIds() == null || request.getEquipmentIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 thiết bị trong phiếu thuê!");
        }

        int rentalDays = calculateRentalDays(request.getStartDate(), request.getExpectedReturnDate());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Set<Long> uniqueEquipmentIds = new LinkedHashSet<>(request.getEquipmentIds());

        RentalOrder order = new RentalOrder();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setCreatedBy(currentUser);
        order.setStartDate(request.getStartDate());
        order.setExpectedReturnDate(request.getExpectedReturnDate());
        order.setRentalDays(rentalDays);
        order.setStatus(RentalOrder.Status.ACTIVE);
        order.setNote(request.getNote());

        BigDecimal totalRentalFee = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;

        for (Long eqId : uniqueEquipmentIds) {
            Equipment equipment = equipmentRepository.findById(eqId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị có ID: " + eqId));

            if (equipment.getStatus() == Equipment.Status.MAINTENANCE) {
                throw new IllegalArgumentException("Thiết bị [" + equipment.getCode() + " - " + equipment.getName()
                        + "] đang trong trạng thái Bảo trì / Hỏng, không thể cho thuê!");
            }

            List<RentalOrder> conflicts = rentalOrderRepository.findConflictingOrdersForEquipment(
                    equipment.getId(), request.getStartDate(), request.getExpectedReturnDate());
            if (!conflicts.isEmpty()) {
                RentalOrder conflictOrder = conflicts.get(0);
                throw new IllegalArgumentException(
                        "Thiết bị [" + equipment.getCode() + " - " + equipment.getName() + "] bị trùng lịch thuê với phiếu "
                        + conflictOrder.getOrderCode() + " của khách hàng " + conflictOrder.getCustomer().getFullName()
                        + " (Từ " + conflictOrder.getStartDate().format(fmt)
                        + " đến " + conflictOrder.getExpectedReturnDate().format(fmt) + ")!");
            }

            BigDecimal dailyRate = equipment.getDailyRate();
            BigDecimal deposit = equipment.getDepositAmount();
            BigDecimal subTotal = dailyRate.multiply(BigDecimal.valueOf(rentalDays));

            RentalOrderItem item = new RentalOrderItem(equipment, dailyRate, deposit, subTotal);
            order.addItem(item);

            totalRentalFee = totalRentalFee.add(subTotal);
            totalDeposit = totalDeposit.add(deposit);

            equipment.setStatus(Equipment.Status.RENTED);
            equipmentRepository.save(equipment);
        }

        order.setTotalRentalFee(totalRentalFee);
        order.setTotalDeposit(totalDeposit);
        order.setFinalTotalAmount(totalRentalFee);
        order.setRefundAmount(totalDeposit);

        RentalOrder savedOrder = rentalOrderRepository.save(order);
        settingService.logActivity(currentUser, "LẬP PHIẾU THUÊ",
                "Lập phiếu thuê " + savedOrder.getOrderCode() + " cho khách hàng " + customer.getFullName());
        return savedOrder;
    }

    public BigDecimal calculateSuggestedLateFee(RentalOrder order, LocalDate actualReturnDate) {
        if (actualReturnDate == null || !actualReturnDate.isAfter(order.getExpectedReturnDate())) {
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(order.getExpectedReturnDate(), actualReturnDate);
        BigDecimal dailyRateSum = order.getDailyRateSum();
        int lateFeePercent = settingService.getInt(SettingService.KEY_LATE_FEE_PERCENT, 150);
        return dailyRateSum.multiply(BigDecimal.valueOf(overdueDays))
                .multiply(BigDecimal.valueOf(lateFeePercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public RentalOrder processReturnOrder(Long orderId, ReturnOrderRequest request) {
        RentalOrder order = getOrderById(orderId);
        if (order.getStatus() != RentalOrder.Status.ACTIVE) {
            throw new IllegalArgumentException("Phiếu thuê " + order.getOrderCode() + " không ở trạng thái Đang thuê!");
        }

        LocalDate actualReturnDate = request.getActualReturnDate() != null
                ? request.getActualReturnDate()
                : LocalDate.now();

        if (actualReturnDate.isBefore(order.getStartDate())) {
            throw new IllegalArgumentException("Ngày trả thực tế không thể nhỏ hơn ngày bắt đầu thuê (" + order.getStartDate() + ")!");
        }

        order.setActualReturnDate(actualReturnDate);

        BigDecimal lateFee = request.getLateFee() != null && request.getLateFee().compareTo(BigDecimal.ZERO) >= 0
                ? request.getLateFee()
                : calculateSuggestedLateFee(order, actualReturnDate);

        BigDecimal totalDamageFee = BigDecimal.ZERO;

        Map<Long, ReturnOrderRequest.ItemReturnDetail> detailMap = new HashMap<>();
        if (request.getItemDetails() != null) {
            for (ReturnOrderRequest.ItemReturnDetail d : request.getItemDetails()) {
                if (d.getItemId() != null) {
                    detailMap.put(d.getItemId(), d);
                }
            }
        }

        order.setStatus(RentalOrder.Status.RETURNED);

        for (RentalOrderItem item : order.getItems()) {
            ReturnOrderRequest.ItemReturnDetail detail = detailMap.get(item.getId());
            String condition = "Bình thường - Đầy đủ phụ kiện";
            BigDecimal itemDamageFee = BigDecimal.ZERO;
            boolean needsMaintenance = false;

            if (detail != null) {
                if (detail.getReturnCondition() != null && !detail.getReturnCondition().trim().isEmpty()) {
                    condition = detail.getReturnCondition().trim();
                }
                if (detail.getDamageFee() != null && detail.getDamageFee().compareTo(BigDecimal.ZERO) > 0) {
                    itemDamageFee = detail.getDamageFee();
                }
                needsMaintenance = detail.isNeedsMaintenance();
            }

            item.setReturnCondition(condition);
            item.setDamageFee(itemDamageFee);
            totalDamageFee = totalDamageFee.add(itemDamageFee);

            Equipment eq = item.getEquipment();
            if (needsMaintenance) {
                eq.setStatus(Equipment.Status.MAINTENANCE);
                eq.setConditionNote("Cần bảo trì sau phiếu " + order.getOrderCode() + ": " + condition);
            } else {
                boolean stillActiveElsewhere = rentalOrderRepository.hasActiveOrdersForEquipment(eq.getId());
                if (!stillActiveElsewhere) {
                    eq.setStatus(Equipment.Status.AVAILABLE);
                }
            }
            equipmentRepository.save(eq);
        }

        order.setLateFee(lateFee);
        order.setDamageFee(totalDamageFee);

        BigDecimal refundAmount = order.getTotalDeposit().subtract(lateFee).subtract(totalDamageFee);
        order.setRefundAmount(refundAmount);

        BigDecimal finalTotal = order.getTotalRentalFee().add(lateFee).add(totalDamageFee);
        order.setFinalTotalAmount(finalTotal);

        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            String existingNote = order.getNote() != null && !order.getNote().isEmpty() ? order.getNote() + " | " : "";
            order.setNote(existingNote + "Ghi chú trả đồ: " + request.getNote().trim());
        }

        RentalOrder saved = rentalOrderRepository.save(order);
        settingService.logActivity(order.getCreatedBy(), "NHẬN TRẢ THIẾT BỊ",
                "Hoàn tất nhận trả & quyết toán phiếu thuê " + saved.getOrderCode());
        return saved;
    }

    private String generateOrderCode() {
        String prefix = "PT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = rentalOrderRepository.count() + 1;
        while (true) {
            String currentCode = prefix + String.format("%03d", count);
            boolean exists = rentalOrderRepository.searchOrders(currentCode, null).stream()
                    .anyMatch(o -> o.getOrderCode().equalsIgnoreCase(currentCode));
            if (!exists) {
                return currentCode;
            }
            count++;
        }
    }
}
