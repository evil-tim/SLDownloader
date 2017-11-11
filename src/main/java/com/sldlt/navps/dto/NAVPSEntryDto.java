package com.sldlt.navps.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class NAVPSEntryDto {

    private String fund;

    private String fundName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal value;

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return fund + " " + date.toString() + " " + value.toPlainString();
    }

}
