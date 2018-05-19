package com.sldlt.orders.service;

import java.util.List;

import com.sldlt.orders.dto.AggregatedOrder;
import com.sldlt.orders.dto.OrderDto;

public interface OrderAggregatorService {

    List<AggregatedOrder> aggregateOrders(List<OrderDto> orders);

}
