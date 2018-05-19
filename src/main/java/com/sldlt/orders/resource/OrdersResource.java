package com.sldlt.orders.resource;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.orders.dto.AggregatedOrder;
import com.sldlt.orders.dto.OrderRequestDto;
import com.sldlt.orders.service.OrderAggregatorService;

@RestController
public class OrdersResource {

    @Autowired
    private OrderAggregatorService orderAggregatorService;

    @RequestMapping(path = "/api/orders/aggregate-actual-orders", method = RequestMethod.POST)
    public List<AggregatedOrder> generateAggregatedOrders(@RequestBody @Valid OrderRequestDto orders) {
        return orderAggregatorService.aggregateOrders(orders.getOrders());
    }

}
