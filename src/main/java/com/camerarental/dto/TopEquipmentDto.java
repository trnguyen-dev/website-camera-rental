package com.camerarental.dto;

import java.math.BigDecimal;

public class TopEquipmentDto {
    private Long id;
    private String code;
    private String name;
    private String brand;
    private String categoryName;
    private long rentalCount;
    private BigDecimal totalGeneratedRevenue;

    public TopEquipmentDto(Long id, String code, String name, String brand,
                           String categoryName, long rentalCount, BigDecimal totalGeneratedRevenue) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.categoryName = categoryName;
        this.rentalCount = rentalCount;
        this.totalGeneratedRevenue = totalGeneratedRevenue;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public long getRentalCount() {
        return rentalCount;
    }

    public BigDecimal getTotalGeneratedRevenue() {
        return totalGeneratedRevenue;
    }
}
