package com.sldlt.orders.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AggregatedOrder implements Comparable<AggregatedOrder> {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Map<String, AggregatedFundOrder> aggregatedFundOrders = new HashMap<>();

    private BigDecimal totalBaseValue = BigDecimal.ZERO;

    private BigDecimal totalActualValue = BigDecimal.ZERO;

    public AggregatedOrder() {
        // nothing to do
    }

    public AggregatedOrder(AggregatedOrder aggregatedOrder, LocalDate date) {
        this.date = date;
        this.totalBaseValue = aggregatedOrder.getTotalBaseValue();
        aggregatedOrder.getAggregatedFundOrders().forEach((code, aggFundOrder) -> {
            aggregatedFundOrders.put(code, new AggregatedFundOrder(aggFundOrder));
        });
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, AggregatedFundOrder> getAggregatedFundOrders() {
        return aggregatedFundOrders;
    }

    public void setAggregatedFundOrders(Map<String, AggregatedFundOrder> aggregatedFundOrders) {
        this.aggregatedFundOrders = aggregatedFundOrders;
    }

    public BigDecimal getTotalBaseValue() {
        return totalBaseValue;
    }

    public void setTotalBaseValue(BigDecimal totalBaseValue) {
        this.totalBaseValue = totalBaseValue;
    }

    public BigDecimal getTotalActualValue() {
        return totalActualValue;
    }

    public void setTotalActualValue(BigDecimal totalActualValue) {
        this.totalActualValue = totalActualValue;
    }

    @Override
    public int compareTo(AggregatedOrder o) {
        return this.date.compareTo(o.getDate());
    }

}
