package com.camerarental.dto;

import java.math.BigDecimal;

public class MonthlyRevenueDto {
    private String monthLabel; // VD: "11/2026"
    private long orderCount;
    private BigDecimal rentalFeeTotal;
    private BigDecimal lateFeeTotal;
    private BigDecimal damageFeeTotal;
    private BigDecimal totalRevenue;

    public MonthlyRevenueDto(String monthLabel, long orderCount,
                             BigDecimal rentalFeeTotal, BigDecimal lateFeeTotal,
                             BigDecimal damageFeeTotal, BigDecimal totalRevenue) {
        this.monthLabel = monthLabel;
        this.orderCount = orderCount;
        this.rentalFeeTotal = rentalFeeTotal;
        this.lateFeeTotal = lateFeeTotal;
        this.damageFeeTotal = damageFeeTotal;
        this.totalRevenue = totalRevenue;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getRentalFeeTotal() {
        return rentalFeeTotal;
    }

    public BigDecimal getLateFeeTotal() {
        return lateFeeTotal;
    }

    public BigDecimal getDamageFeeTotal() {
        return damageFeeTotal;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
