package com.sldlt.navps.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class NAVPSPredictionDto implements Comparable<NAVPSPredictionDto> {

    private Long id;

    private String fund;

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer daysInAdvance;

    private BigDecimal value;

    private String parameters;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getDaysInAdvance() {
        return daysInAdvance;
    }

    public void setDaysInAdvance(Integer daysInAdvance) {
        this.daysInAdvance = daysInAdvance;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return type + "\n" + fund + " " + date + " +" + daysInAdvance + "\n" + value + "\n" + parameters;
    }

    @Override
    public int compareTo(NAVPSPredictionDto other) {
        if (other == null) {
            return -1;
        }

        int compare = 0;
        compare = this.getDate().compareTo(other.getDate());
        if (compare != 0) {
            return compare;
        }

        compare = this.getDaysInAdvance().compareTo(other.getDaysInAdvance());
        if (compare != 0) {
            return compare;
        }

        compare = this.getFund().compareTo(other.getFund());
        if (compare != 0) {
            return compare;
        }

        compare = this.getType().compareTo(other.getType());
        if (compare != 0) {
            return compare;
        }

        return compare;
    }

}
