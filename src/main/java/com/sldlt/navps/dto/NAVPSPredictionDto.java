package com.sldlt.navps.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

public class NAVPSPredictionDto implements Comparable<NAVPSPredictionDto> {

    private Long id;

    private String fund;

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate predictionDate;

    private Integer daysInAdvance;

    private BigDecimal predictionValue;

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

    public LocalDate getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }

    public Integer getDaysInAdvance() {
        return daysInAdvance;
    }

    public void setDaysInAdvance(Integer daysInAdvance) {
        this.daysInAdvance = daysInAdvance;
    }

    public BigDecimal getPredictionValue() {
        return predictionValue;
    }

    public void setPredictionValue(BigDecimal predictionValue) {
        this.predictionValue = predictionValue;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(type).append("\n").append(fund).append(" ").append(predictionDate).append(" +")
            .append(daysInAdvance).append("\n").append(predictionValue).toString();
    }

    @Override
    public int compareTo(NAVPSPredictionDto other) {
        if (other == null) {
            return -1;
        }

        int compare = 0;
        compare = this.getPredictionDate().compareTo(other.getPredictionDate());
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NAVPSPredictionDto)) {
            return false;
        }
        NAVPSPredictionDto other = (NAVPSPredictionDto) obj;
        return compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPredictionDate(), this.getDaysInAdvance(), this.getFund(), this.getType());
    }

}
