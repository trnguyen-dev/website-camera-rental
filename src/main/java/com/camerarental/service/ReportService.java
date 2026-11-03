package com.camerarental.service;

import com.camerarental.dto.MonthlyRevenueDto;
import com.camerarental.dto.TopEquipmentDto;
import com.camerarental.entity.Equipment;
import com.camerarental.entity.RentalOrder;
import com.camerarental.repository.CustomerRepository;
import com.camerarental.repository.EquipmentRepository;
import com.camerarental.repository.RentalOrderItemRepository;
import com.camerarental.repository.RentalOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final RentalOrderRepository rentalOrderRepository;
    private final RentalOrderItemRepository rentalOrderItemRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerRepository customerRepository;

    public ReportService(RentalOrderRepository rentalOrderRepository,
                         RentalOrderItemRepository rentalOrderItemRepository,
                         EquipmentRepository equipmentRepository,
                         CustomerRepository customerRepository) {
        this.rentalOrderRepository = rentalOrderRepository;
        this.rentalOrderItemRepository = rentalOrderItemRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * 1. Thống kê doanh thu theo tháng (Sắp xếp từ tháng mới nhất về trước)
     */
    public List<MonthlyRevenueDto> getMonthlyRevenues() {
        List<RentalOrder> allOrders = rentalOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() != RentalOrder.Status.CANCELLED)
                .collect(Collectors.toList());

        Map<YearMonth, List<RentalOrder>> grouped = allOrders.stream()
                .collect(Collectors.groupingBy(o -> YearMonth.from(o.getStartDate())));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<RentalOrder>>comparingByKey().reversed())
                .map(entry -> {
                    YearMonth ym = entry.getKey();
                    List<RentalOrder> orders = entry.getValue();
                    long count = orders.size();
                    BigDecimal rentalSum = orders.stream()
                            .map(RentalOrder::getTotalRentalFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal lateSum = orders.stream()
                            .map(RentalOrder::getLateFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal damageSum = orders.stream()
                            .map(RentalOrder::getDamageFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalSum = orders.stream()
                            .map(RentalOrder::getFinalTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new MonthlyRevenueDto("Tháng " + ym.format(fmt), count, rentalSum, lateSum, damageSum, totalSum);
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. Danh sách phiếu quá hạn chưa trả
     */
    public List<RentalOrder> getOverdueOrders() {
        return rentalOrderRepository.findOverdueOrders(LocalDate.now());
    }

    /**
     * 3. Thiết bị được thuê nhiều nhất
     */
    public List<TopEquipmentDto> getTopRentedEquipments() {
        List<Object[]> rawRows = rentalOrderItemRepository.findTopRentedEquipments();
        List<TopEquipmentDto> result = new ArrayList<>();
        for (Object[] row : rawRows) {
            Long id = (Long) row[0];
            String code = (String) row[1];
            String name = (String) row[2];
            String brand = (String) row[3];
            String categoryName = (String) row[4];
            long count = ((Number) row[5]).longValue();
            BigDecimal totalRev = row[6] instanceof BigDecimal ? (BigDecimal) row[6] : new BigDecimal(row[6].toString());
            result.add(new TopEquipmentDto(id, code, name, brand, categoryName, count, totalRev));
        }
        return result;
    }

    public BigDecimal getTotalSystemRevenue() {
        return rentalOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() != RentalOrder.Status.CANCELLED)
                .map(RentalOrder::getFinalTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTotalEquipmentCount() {
        return equipmentRepository.count();
    }

    public long getAvailableEquipmentCount() {
        return equipmentRepository.searchAndFilter(null, null, Equipment.Status.AVAILABLE).size();
    }

    public long getActiveOrderCount() {
        return rentalOrderRepository.searchOrders(null, RentalOrder.Status.ACTIVE).size();
    }

    public long getTotalCustomerCount() {
        return customerRepository.count();
    }
}
