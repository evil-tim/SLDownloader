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
        }, buildAccmulatedOrders(orderData));
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
        var accumulatedOrderData = buildAccmulatedOrders(orderData);
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

function buildAccmulatedOrders(orders) {
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
                    orderDateObj : currentDate,
                    baseValue : accumulatedOrders[accumulatedOrderIndex].baseValue,
                    baseValuesPerFund : accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund,
                    sharesPerFund : accumulatedOrders[accumulatedOrderIndex].sharesPerFund,
                });
    }

    return accumulatedOrders;
}

var navpsCache = {};

function getOrdersWithHistoricalValues(callback, accumulatedOrders) {
    var navpsRequests = [];
    accumulatedOrders.forEach(function(order) {
        if (order.hasOrder) {
            Object.keys(order.baseValuesPerFund).forEach(
                    function(fundCode) {
                        var dateStr = order.orderDateObj.toISOString().slice(0,
                                10);
                        if (!navpsCache || !navpsCache[fundCode]
                                || !navpsCache[fundCode][dateStr]) {
                            navpsRequests.push($.ajax({
                                url : "/api/navps",
                                data : {
                                    sort : "date,desc",
                                    size : 1,
                                    fund : fundCode,
                                    dateFrom : dateStr,
                                    dateTo : dateStr,
                                }
                            }));
                        }
                    });
        }
    });

    var buildFunction = function(bfCallback, cache, orders) {
        orders
                .forEach(function(order, index) {
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
                    } else {
                        if (orders[index - 1]) {
                            order.actualValue = orders[index - 1].actualValue
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
                                        .forEach(function(navpsLookupResult) {
                                            if (navpsLookupResult
                                                    && navpsLookupResult[0]
                                                    && navpsLookupResult[0].content
                                                    && navpsLookupResult[0].content[0]
                                                    && navpsLookupResult[0].content[0].fund
                                                    && navpsLookupResult[0].content[0].date
                                                    && navpsLookupResult[0].content[0].value) {
                                                var lookUpFundCode = navpsLookupResult[0].content[0].fund;
                                                var lookUpFundDate = navpsLookupResult[0].content[0].date;
                                                var lookUpFundValue = navpsLookupResult[0].content[0].value;
                                                if (!navpsCache[lookUpFundCode]) {
                                                    navpsCache[lookUpFundCode] = [];
                                                }
                                                navpsCache[lookUpFundCode][lookUpFundDate] = lookUpFundValue;
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
