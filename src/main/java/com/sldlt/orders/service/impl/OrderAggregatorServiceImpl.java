package com.sldlt.orders.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSService;
import com.sldlt.orders.dto.AggregatedFundOrder;
import com.sldlt.orders.dto.AggregatedOrder;
import com.sldlt.orders.dto.OrderDto;
import com.sldlt.orders.service.OrderAggregatorService;

@Service
public class OrderAggregatorServiceImpl implements OrderAggregatorService {

    @Autowired
    private NAVPSService navpsService;

    @Override
    public List<AggregatedOrder> aggregateOrders(final List<OrderDto> orders) {
        return computeActualValues(aggregateNavpsAndBaseValues(createTimeAggregatedOrders(orders)));
    }

    private List<AggregatedOrder> computeActualValues(final List<AggregatedOrder> aggregatedOrders) {
        IntStream.range(1, aggregatedOrders.size()).mapToObj(index -> Pair.of(aggregatedOrders.get(index - 1), aggregatedOrders.get(index)))
            .forEach(intervalPair -> {
                final AggregatedOrder intervalStartOrder = intervalPair.getFirst();
                final AggregatedOrder intervalEndOrder = intervalPair.getSecond();
                final Map<LocalDate, AggregatedOrder> fillerOrders = new HashMap<>();
                intervalStartOrder.getAggregatedFundOrders().forEach((fund, aggregatedFundOrder) -> {
                    final List<NAVPSEntryDto> navpsList = navpsService.listNAVPS(fund, intervalStartOrder.getDate(),
                        intervalEndOrder.getDate().minusDays(1), false);
                    navpsList.forEach(navpsEntry -> {
                        if (intervalStartOrder.getDate().equals(navpsEntry.getDate())) {
                            aggregateActualValueForNavps(intervalStartOrder, fund, navpsEntry.getValue());
                        } else {
                            if (!fillerOrders.containsKey(navpsEntry.getDate())) {
                                fillerOrders.put(navpsEntry.getDate(), new AggregatedOrder(intervalStartOrder, navpsEntry.getDate()));
                            }
                            aggregateActualValueForNavps(fillerOrders.get(navpsEntry.getDate()), fund, navpsEntry.getValue());
                        }
                    });
                });
                aggregatedOrders.addAll(fillerOrders.values());
            });

        // sort time aggregated orders
        Collections.sort(aggregatedOrders);
        if (!aggregatedOrders.isEmpty()
            && aggregatedOrders.get(aggregatedOrders.size() - 1).getTotalActualValue().equals(BigDecimal.ZERO)) {
            aggregatedOrders.remove(aggregatedOrders.size() - 1);
        }

        return aggregatedOrders;
    }

    private void aggregateActualValueForNavps(final AggregatedOrder aggregatedOrder, final String fund, final BigDecimal navps) {
        final AggregatedFundOrder aggregatedFundOrder = aggregatedOrder.getAggregatedFundOrders().get(fund);
        final BigDecimal fundActualValue = navps.multiply(aggregatedFundOrder.getShares());
        aggregatedFundOrder.setActualValue(fundActualValue);
        aggregatedOrder.setTotalActualValue(aggregatedOrder.getTotalActualValue().add(fundActualValue));
    }

    private List<AggregatedOrder> aggregateNavpsAndBaseValues(final List<AggregatedOrder> aggregatedOrders) {
        // sort time aggregated orders
        Collections.sort(aggregatedOrders);

        for (int i = 0; i < aggregatedOrders.size(); i++) {
            if (i > 0) {
                // add prev aggregated order to current one
                addPrevAggregatedFundOrder(aggregatedOrders.get(i), aggregatedOrders.get(i - 1));
            }

            // compute total base value
            aggregatedOrders.get(i).setTotalBaseValue(aggregatedOrders.get(i).getAggregatedFundOrders().values().stream()
                .map(AggregatedFundOrder::getBaseValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        }

        if (!aggregatedOrders.isEmpty()) {
            // add empty aggregated order
            final AggregatedOrder startAggregatedOrder = new AggregatedOrder();
            startAggregatedOrder.setDate(aggregatedOrders.get(0).getDate().minusDays(1));
            aggregatedOrders.add(0, startAggregatedOrder);

            // add end aggregated order
            aggregatedOrders.add(new AggregatedOrder(aggregatedOrders.get(aggregatedOrders.size() - 1), LocalDate.now()));
        }
        return aggregatedOrders;
    }

    private void addPrevAggregatedFundOrder(final AggregatedOrder aggregatedOrder, final AggregatedOrder prevAggregatedOrder) {
        final Map<String, AggregatedFundOrder> prevAggFundOrders = prevAggregatedOrder.getAggregatedFundOrders();

        // get each individual fund order
        prevAggFundOrders.forEach((code, prevFundAggOrder) -> {
            AggregatedFundOrder currentAggFundOrder = aggregatedOrder.getAggregatedFundOrders().get(code);
            if (currentAggFundOrder == null) {
                // copy prev if doesn't exist in current
                currentAggFundOrder = new AggregatedFundOrder(prevFundAggOrder);
                aggregatedOrder.getAggregatedFundOrders().put(code, currentAggFundOrder);
            } else {
                // add to current
                currentAggFundOrder.setShares(currentAggFundOrder.getShares().add(prevFundAggOrder.getShares()));
                currentAggFundOrder.setBaseValue(currentAggFundOrder.getBaseValue().add(prevFundAggOrder.getBaseValue()));
            }
        });

    }

    private List<AggregatedOrder> createTimeAggregatedOrders(final List<OrderDto> orders) {
        // build initial aggregated orders from individual orders
        final List<AggregatedOrder> aggregatedOrders = new LinkedList<>();

        orders.forEach(order -> {
            // get aggregated order if it exists
            Optional<AggregatedOrder> matchingAggregatedOrder = aggregatedOrders.stream()
                .filter(aggOrder -> aggOrder.getDate().equals(order.getDate())).findAny();

            // create empty aggregated order for date if nonexistent
            if (!matchingAggregatedOrder.isPresent()) {
                final AggregatedOrder newAggOrder = new AggregatedOrder();
                newAggOrder.setDate(order.getDate());
                aggregatedOrders.add(newAggOrder);
                matchingAggregatedOrder = Optional.of(newAggOrder);
            }

            matchingAggregatedOrder.ifPresent(aggOrder -> {
                // get aggregated fund order if it exists
                AggregatedFundOrder matchingAggregatedFundOrder = aggOrder.getAggregatedFundOrders().get(order.getCode());

                // create empty aggregated fund order if nonexistent
                if (matchingAggregatedFundOrder == null) {
                    matchingAggregatedFundOrder = new AggregatedFundOrder();
                    matchingAggregatedFundOrder.setCode(order.getCode());
                    aggOrder.getAggregatedFundOrders().put(order.getCode(), matchingAggregatedFundOrder);
                }

                matchingAggregatedFundOrder.setBaseValue(matchingAggregatedFundOrder.getBaseValue().add(order.getBaseValue()));
                matchingAggregatedFundOrder.setShares(matchingAggregatedFundOrder.getShares().add(order.getShares()));
            });
        });

        return aggregatedOrders;
    }

}
