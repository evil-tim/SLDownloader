package com.sldlt.orders.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class OrderDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate date;

    @NotNull
    private String code;

    @NotNull
    private BigDecimal shares;

    private BigDecimal baseValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public void setShares(BigDecimal shares) {
        this.shares = shares;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(BigDecimal baseValue) {
        this.baseValue = baseValue;
    }

    @AssertTrue
    public boolean isBaseValueValid() {
        if (this.shares != null && this.shares.compareTo(BigDecimal.ZERO) >= 0) {
            return this.baseValue != null;
        } else if (this.shares != null && this.shares.compareTo(BigDecimal.ZERO) < 0) {
            return this.baseValue == null;
        }
        return false;
    }
}
