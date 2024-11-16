package com.sldlt.navps.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class NAVPSEntryDto {

    private String fund;

    private String fundName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private BigDecimal fundValue;

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

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public BigDecimal getFundValue() {
        return fundValue;
    }

    public void setFundValue(BigDecimal fundValue) {
        this.fundValue = fundValue;
    }

    @Override
    public String toString() {
        return fund + " - " + entryDate.toString() + " - " + fundValue.toPlainString();
    }

}
