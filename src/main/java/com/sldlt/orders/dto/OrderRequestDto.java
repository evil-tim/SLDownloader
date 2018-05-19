package com.sldlt.orders.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OrderRequestDto {

    @NotNull
    @Valid
    private List<OrderDto> orders;

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

}
