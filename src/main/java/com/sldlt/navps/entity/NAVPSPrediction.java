package com.sldlt.navps.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class NAVPSPrediction implements Serializable {

    private static final long serialVersionUID = 4200496311440169602L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fund;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDate predictionDate;

    @Column(nullable = false)
    private Integer daysInAdvance;

    @Column(nullable = false, precision = 10, scale = 6)
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

}
