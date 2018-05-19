package com.sldlt.orders.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OrderDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate date;

    @NotNull
    private String code;

    @NotNull
    private Long shares;

    @NotNull
    private BigDecimal baseValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
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

}
