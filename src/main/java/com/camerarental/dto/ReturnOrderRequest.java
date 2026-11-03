package com.camerarental.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReturnOrderRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate actualReturnDate;

    private BigDecimal lateFee = BigDecimal.ZERO;

    private String note;

    private List<ItemReturnDetail> itemDetails = new ArrayList<>();

    public static class ItemReturnDetail {
        private Long itemId;
        private String returnCondition;
        private BigDecimal damageFee = BigDecimal.ZERO;
        private boolean needsMaintenance = false;

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getReturnCondition() {
            return returnCondition;
        }

        public void setReturnCondition(String returnCondition) {
            this.returnCondition = returnCondition;
        }

        public BigDecimal getDamageFee() {
            return damageFee;
        }

        public void setDamageFee(BigDecimal damageFee) {
            this.damageFee = damageFee;
        }

        public boolean isNeedsMaintenance() {
            return needsMaintenance;
        }

        public void setNeedsMaintenance(boolean needsMaintenance) {
            this.needsMaintenance = needsMaintenance;
        }
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ItemReturnDetail> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<ItemReturnDetail> itemDetails) {
        this.itemDetails = itemDetails;
    }
}
