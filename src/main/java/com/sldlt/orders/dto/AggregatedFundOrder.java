package com.sldlt.orders.dto;

import java.math.BigDecimal;

public class AggregatedFundOrder {

    private String code;

    private BigDecimal shares = BigDecimal.ZERO;

    private BigDecimal baseValue = BigDecimal.ZERO;

    private BigDecimal actualValue = BigDecimal.ZERO;

    public AggregatedFundOrder() {
        // nothing to do
    }

    public AggregatedFundOrder(AggregatedFundOrder aggregatedFundOrder) {
        this.code = aggregatedFundOrder.getCode();
        this.shares = aggregatedFundOrder.getShares();
        this.baseValue = aggregatedFundOrder.getBaseValue();
    }

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

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(BigDecimal baseValue) {
        this.baseValue = baseValue;
    }

    public BigDecimal getActualValue() {
        return actualValue;
    }

    public void setActualValue(BigDecimal actualValue) {
        this.actualValue = actualValue;
    }

}
