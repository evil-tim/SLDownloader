google.charts.load('current', {
    'packages' : [ 'annotationchart', 'corechart' ]
});
google.charts.setOnLoadCallback(initOrdersChart);

$(document).ready(function() {
    addOrderUpdateCallback(updateOrdersValueChart);
    addOrderUpdateCallback(updateOrdersSharesChart);
});

function initOrdersChart() {
    updateOrdersValueChart(getOrders());
    updateOrdersSharesChart(getOrders());
}

function updateOrdersValueChart(rawOrderData) {
    var chartData = new google.visualization.DataTable();
    chartData.addColumn('date', 'Date');
    chartData.addColumn('number', 'Base Value');
    chartData.addColumn('number', 'Actual Value');

    getOrdersWithCurrentValues(function(orderData) {
        getOrdersWithHistoricalValues(function(accumulatedOrderData) {
            for (var i = 0; i < accumulatedOrderData.length; i++) {
                chartData.addRow([ accumulatedOrderData[i].orderDateObj,
                        accumulatedOrderData[i].baseValue,
                        accumulatedOrderData[i].actualValue ]);
            }
            drawValuesChart(chartData);
        }, addFillerEntries(buildAccumulatedOrders(orderData)));
    }, rawOrderData);
}

function drawValuesChart(chartData) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('ordersChart'));
    var options = {
        displayAnnotations : false
    };
    chart.draw(chartData, options);
}

function updateOrdersSharesChart(rawOrderData) {
    var chartData = new google.visualization.DataTable();
    chartData.addColumn('date', 'Date');

    var uniqueFunds = [];
    var uniqueFundCodes = [];
    rawOrderData.forEach(function(rawOrder) {
        if (!uniqueFunds[rawOrder.orderFundCode]) {
            uniqueFundCodes.push(rawOrder.orderFundCode);
            uniqueFunds[rawOrder.orderFundCode] = rawOrder.orderFundName;
        }
    });
    uniqueFundCodes.sort();
    uniqueFundCodes.forEach(function(fundCode) {
        chartData.addColumn('number', uniqueFunds[fundCode]);
    });

    getOrdersWithCurrentValues(function(orderData) {
        var accumulatedOrderData = buildAccumulatedOrders(orderData);
        accumulatedOrderData.forEach(function(accumulatedOrder) {
            var row = [ accumulatedOrder.orderDateObj ];
            uniqueFundCodes.forEach(function(fundCode) {
                row.push(accumulatedOrder.sharesPerFund[fundCode]);
            });
            chartData.addRow(row);
        });

        drawSharesChart(chartData);
    }, rawOrderData);
}

function drawSharesChart(chartData) {
    var chart = new google.visualization.AreaChart(document
            .getElementById('ordersSharesChart'));
    var options = {
        isStacked : true
    };
    chart.draw(chartData, options);
}

function buildAccumulatedOrders(orders) {
    var accumulatedOrders = [];
    var accumulatedOrderIndex = -1;

    var sortedOrders = [];
    // copy orders and set additional data
    orders.forEach(function(order) {
        var copiedOrder = Object.assign({}, order);
        copiedOrder.orderDateObj = new Date(order.orderDate);
        sortedOrders.push(copiedOrder);
    });
    // sort orders
    sortedOrders.sort(function(a, b) {
        return a.orderDateObj < b.orderDateObj ? -1
                : (a.orderDateObj > b.orderDateObj ? 1 : 0);
    });

    // accumulate orders
    sortedOrders
            .forEach(function(order) {
                // create new entry if needed
                if (accumulatedOrderIndex == -1
                        || accumulatedOrders[accumulatedOrderIndex].orderDateObj < order.orderDateObj) {

                    // create new entry for day before current order if needed
                    var prevDate = new Date(order.orderDateObj);
                    prevDate.setDate(prevDate.getDate() - 1);
                    if (accumulatedOrderIndex == -1
                            || accumulatedOrders[accumulatedOrderIndex].orderDateObj
                                    .getTime() !== prevDate.getTime()) {
                        accumulatedOrders
                                .push({
                                    hasOrder : false,
                                    isFiller : false,
                                    orderDateObj : prevDate,
                                    baseValue : accumulatedOrderIndex == -1 ? 0
                                            : accumulatedOrders[accumulatedOrderIndex].baseValue,
                                    baseValuesPerFund : accumulatedOrderIndex == -1 ? {}
                                            : Object
                                                    .assign(
                                                            {},
                                                            accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund),
                                    sharesPerFund : accumulatedOrderIndex == -1 ? {}
                                            : Object
                                                    .assign(
                                                            {},
                                                            accumulatedOrders[accumulatedOrderIndex].sharesPerFund),
                                });
                        accumulatedOrderIndex++;
                    }

                    // create new entry based on current order
                    accumulatedOrders
                            .push({
                                hasOrder : true,
                                isFiller : false,
                                orderDateObj : order.orderDateObj,
                                baseValue : accumulatedOrders[accumulatedOrderIndex].baseValue,
                                baseValuesPerFund : Object
                                        .assign(
                                                {},
                                                accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund),
                                sharesPerFund : Object
                                        .assign(
                                                {},
                                                accumulatedOrders[accumulatedOrderIndex].sharesPerFund),
                            });
                    accumulatedOrderIndex++;
                }

                // accumulate values to current entry
                // accumulate base value
                accumulatedOrders[accumulatedOrderIndex].baseValue = accumulatedOrders[accumulatedOrderIndex].baseValue
                        + order.orderValue;
                // accumulate base value per fund
                if (accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode]) {
                    // add base value if existing
                    accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode] = accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode]
                            + order.orderValue;
                } else {
                    // create new entry
                    accumulatedOrders[accumulatedOrderIndex - 1].baseValuesPerFund[order.orderFundCode] = 0;
                    accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode] = order.orderValue;
                }
                // accumulate shares per fund
                if (accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode]) {
                    // add shares if existing
                    accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode] = accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode]
                            + order.orderShares;
                } else {
                    // create new entry
                    accumulatedOrders[accumulatedOrderIndex - 1].sharesPerFund[order.orderFundCode] = 0;
                    accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode] = order.orderShares;
                }

            });

    // create end entry
    var currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);
    currentDate.setMinutes(currentDate.getMinutes()
            - currentDate.getTimezoneOffset());
    if (accumulatedOrderIndex >= 0
            && accumulatedOrders[accumulatedOrderIndex].orderDateObj.getTime() !== currentDate
                    .getTime()) {
        accumulatedOrders
                .push({
                    hasOrder : false,
                    isFiller : false,
                    orderDateObj : currentDate,
                    baseValue : accumulatedOrders[accumulatedOrderIndex].baseValue,
                    baseValuesPerFund : accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund,
                    sharesPerFund : accumulatedOrders[accumulatedOrderIndex].sharesPerFund,
                });
    }

    return accumulatedOrders;
}

function addFillerEntries(orders) {
    var fillerOrders = [];
    var prevOrder = null;
    orders.forEach(function(order) {
        // add filler entries between each order entry
        if (prevOrder != null) {
            var diffDays = Math.ceil(Math.abs(order.orderDateObj.getTime()
                    - prevOrder.orderDateObj.getTime()) / 86400000);
            // add filler entries only if difference > 1 week
            if(diffDays >= 7) {

                // compute and add each filler entry
                var fillerDate = new Date(prevOrder.orderDateObj);
                fillerDate.setDate(fillerDate.getDate() - fillerDate.getDay());

                while(true) {
                    var fillerEndWeekDate = new Date(fillerDate);
                    fillerEndWeekDate.setDate(fillerEndWeekDate.getDate() + 6);

                    if(fillerEndWeekDate.getTime() >= order.orderDateObj.getTime()) {
                        break;
                    }

                    fillerOrders.push({
                        hasOrder : false,
                        isFiller : true,
                        orderDateObj : null,
                        fillerDateFromObj : new Date(fillerDate),
                        fillerDateToObj : fillerEndWeekDate,
                        baseValue : prevOrder.baseValue,
                        baseValuesPerFund : prevOrder.baseValuesPerFund,
                        sharesPerFund : prevOrder.sharesPerFund,
                    });

                    fillerDate.setDate(fillerDate.getDate() + 7);
                }
            }
        }
        fillerOrders.push(order);
        prevOrder = order;
    });
    return fillerOrders;
}

var navpsCacheAvailable = {};
var navpsCache = {};

function getOrdersWithHistoricalValues(callback, accumulatedOrders) {
    var navpsRequests = [];
    accumulatedOrders.forEach(function(order) {
        if (order.hasOrder || order.isFiller) {
            Object.keys(order.baseValuesPerFund).forEach(
                    function(fundCode) {
                        var dateFromStr = null;
                        var dateToStr = null;
                        if(order.isFiller) {
                            dateFromStr = order.fillerDateFromObj.toISOString().slice(0,
                                    10);
                            dateToStr = order.fillerDateToObj.toISOString().slice(0,
                                    10);
                        } else {
                            var orderDateObj = new Date(order.orderDateObj);

                            orderDateObj.setDate(orderDateObj.getDate() - orderDateObj.getDay());
                            dateFromStr = orderDateObj.toISOString().slice(0,
                                    10);

                            orderDateObj.setDate(orderDateObj.getDate() + 6);
                            dateToStr = orderDateObj.toISOString().slice(0,
                                    10);
                        }

                        var requestRangeKey = dateFromStr + "-" + dateToStr;

                        if (!navpsCacheAvailable || !navpsCacheAvailable[fundCode]
                                || !navpsCacheAvailable[fundCode][requestRangeKey]) {
                            navpsRequests.push($.ajax({
                                url : "/api/navps",
                                data : {
                                    sort : "date,desc",
                                    size : 7,
                                    fund : fundCode,
                                    dateFrom : dateFromStr,
                                    dateTo : dateToStr,
                                }
                            }));
                            if(!navpsCacheAvailable) {
                                navpsCacheAvailable = {};
                            }
                            if(!navpsCacheAvailable[fundCode]) {
                                navpsCacheAvailable[fundCode] = [];
                            }
                            if(!navpsCacheAvailable[fundCode][requestRangeKey]) {
                                navpsCacheAvailable[fundCode][requestRangeKey] = true;
                            }
                        }
                    });
        }
    });

    var buildFunction = function(bfCallback, cache, orders) {
        orders
                .forEach(function(order, index) {
                    var hasActualValue = false;
                    order.actualValue = 0;
                    if (order.hasOrder) {
                        var dateStr = order.orderDateObj.toISOString().slice(0,
                                10);
                        var accumulator = 0;
                        Object
                                .keys(order.baseValuesPerFund)
                                .forEach(
                                        function(fundCode) {
                                            if (cache[fundCode]
                                                    && cache[fundCode][dateStr]) {
                                                accumulator = accumulator
                                                        + (cache[fundCode][dateStr] * order.sharesPerFund[fundCode]);
                                            }
                                        });
                        order.actualValue = accumulator;
                        hasActualValue = true;
                    } else if(order.isFiller) {
                        var fillerDate = new Date(order.fillerDateToObj);
                        fillerDate.setDate(fillerDate.getDate() - 1);
                        while(fillerDate.getDay() > 0) {
                            var dateStr = fillerDate.toISOString().slice(0,
                                    10);
                            var accumulator = 0;
                            Object
                                    .keys(order.baseValuesPerFund)
                                    .forEach(
                                            function(fundCode) {
                                                if (cache[fundCode]
                                                        && cache[fundCode][dateStr]) {
                                                    hasActualValue = true;
                                                    accumulator = accumulator
                                                            + (cache[fundCode][dateStr] * order.sharesPerFund[fundCode]);
                                                }
                                            });
                            if(hasActualValue) {
                                order.orderDateObj = fillerDate;
                                order.actualValue = accumulator;
                                break;
                            }
                            fillerDate.setDate(fillerDate.getDate() - 1);
                        }
                    }

                    if(!hasActualValue) {
                        if (orders[index - 1]) {
                            order.actualValue = orders[index - 1].actualValue
                        }
                        if(!order.orderDateObj && order.fillerDateToObj) {
                            var fallbackDate = new Date(order.fillerDateToObj);
                            fallbackDate.setDate(fallbackDate.getDate() - 1);
                            order.orderDateObj = fallbackDate;
                        }
                    }
                });
        bfCallback(orders);
    };

    if (navpsRequests.length > 0) {
        $.when
                .apply($, navpsRequests)
                .then(
                        function(...navpsLookupResults) {
                            if (navpsLookupResults) {
                                if (navpsRequests.length == 1) {
                                    navpsLookupResults = [ navpsLookupResults ];
                                }
                                navpsLookupResults
                                        .forEach(function(navpsLookupResult, requestIndex) {
                                            if (navpsLookupResult
                                                    && navpsLookupResult[0]
                                                    && navpsLookupResult[0].content) {
                                                navpsLookupResult[0].content.forEach(function(navpsEntry) {
                                                    if(navpsEntry && navpsEntry.fund && navpsEntry.date && navpsEntry.value) {
                                                        var lookUpFundCode = navpsEntry.fund;
                                                        var lookUpFundDate = navpsEntry.date;
                                                        var lookUpFundValue = navpsEntry.value;
                                                        if (!navpsCache[lookUpFundCode]) {
                                                            navpsCache[lookUpFundCode] = [];
                                                        }
                                                        navpsCache[lookUpFundCode][lookUpFundDate] = lookUpFundValue;
                                                    }
                                                });
                                            }
                                        });
                            }
                            buildFunction(callback, navpsCache,
                                    accumulatedOrders);
                        });
    } else {
        buildFunction(callback, navpsCache, accumulatedOrders);
    }
}
