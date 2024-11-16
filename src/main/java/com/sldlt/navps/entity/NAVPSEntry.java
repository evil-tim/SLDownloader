package com.sldlt.navps.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = @Index(name = "idx_navps_fund_date", columnList = "fund, entry_date"))
public class NAVPSEntry implements Serializable {

    private static final long serialVersionUID = -3464455970423863740L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fund;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal fundValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
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
