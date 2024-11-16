package com.sldlt.orders.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.metrics.annotation.Instrumented;
import com.sldlt.orders.dto.AggregatedOrder;
import com.sldlt.orders.dto.OrderRequestDto;
import com.sldlt.orders.service.OrderAggregatorService;

import jakarta.validation.Valid;

@RestController
public class OrdersResource {

    @Autowired
    private OrderAggregatorService orderAggregatorService;

    @PostMapping("/api/orders/aggregate-actual-orders")
    @Instrumented
    public List<AggregatedOrder> generateAggregatedOrders(@RequestBody @Valid OrderRequestDto orders) {
        return orderAggregatorService.aggregateOrders(orders.getOrders());
    }

}
