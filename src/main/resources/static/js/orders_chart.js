google.charts.load('current', {
    'packages' : [ 'annotationchart', 'corechart' ]
});
google.charts.setOnLoadCallback(initOrdersChart);

$(document).ready(function() {
    addOrderUpdateCallback(updateOrdersValueChart);
    addOrderUpdateCallback(updateOrdersSharesChart);
    addOrderUpdateCallback(updateOrdersSplitChart);
});

function initOrdersChart() {
    updateOrdersValueChart(getOrders());
    updateOrdersSharesChart(getOrders());
    updateOrdersSplitChart(getOrders());
}

function updateOrdersValueChart(rawOrderData) {
    rawOrderData = rawOrderData ? rawOrderData : getOrders();
    var requestOrderData = [];

    rawOrderData.forEach(function(rawOrder) {
        requestOrderData.push({
            'date' : rawOrder.orderDate,
            'code' : rawOrder.orderFundCode,
            'shares' : rawOrder.orderShares,
            'baseValue' : rawOrder.orderValue
        });
    });

    var chartData = new google.visualization.DataTable();
    chartData.addColumn('date', 'Date');
    chartData.addColumn('number', 'Base Value');
    chartData.addColumn('number', 'Actual Value');

    $.ajax({
        url : "/api/orders/aggregate-actual-orders",
        method : "POST",
        contentType : "application/json; charset=utf-8",
        data : JSON.stringify({
            orders : requestOrderData
        })
    }).done(
            function(data) {
                if (data && Array.isArray(data)) {
                    data.forEach(function(aggregatedOrder) {
                        chartData.addRow([ new Date(aggregatedOrder.date),
                                aggregatedOrder.totalBaseValue,
                                aggregatedOrder.totalActualValue ])
                    });
                }
                drawValuesChart(chartData);
            });
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
    rawOrderData = rawOrderData ? rawOrderData : getOrders();

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
        isStacked : true,
        legend : {
            position : 'bottom',
        },
        chartArea : {
            left : 50,
            width: '100%',
        }
    };
    chart.draw(chartData, options);
}

function updateOrdersSplitChart(rawOrderData) {
    rawOrderData = rawOrderData ? rawOrderData : getOrders();

    var chartData = new google.visualization.DataTable();
    chartData.addColumn('string', 'Fund');
    chartData.addColumn('number', 'Value');    

    getOrdersWithCurrentValues(function(orderData) {
        var orderAccumulator = {};
        orderData.forEach(function(order) {
            if (!orderAccumulator[order.orderFundCode]) {
                orderAccumulator[order.orderFundCode] = [order.orderFundName, new Big(0)];
            }
            orderAccumulator[order.orderFundCode][1] = orderAccumulator[order.orderFundCode][1]
                .plus(order.currentValue);
        });

        Object.keys(orderAccumulator).sort().forEach(function(key) {
            orderAccumulator[key][1] = Number.parseFloat(orderAccumulator[key][1].toFixed(2));
            chartData.addRow(orderAccumulator[key]);
        });

        drawSplitChart(chartData);
    }, rawOrderData);
}

function drawSplitChart(chartData) {
    var chart = new google.visualization.PieChart(document
            .getElementById('ordersSplitChart'));
    var options = {
        chartArea : {
            left : 10,
            width: '100%',
        }
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
                                    orderDateObj : prevDate,
                                    baseValue : accumulatedOrderIndex == -1 ? new Big(0)
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
                        .plus(order.orderValue);
                // accumulate base value per fund
                if (accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode]) {
                    // add base value if existing
                    accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode] = accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode]
                            .plus(order.orderValue);
                } else {
                    // create new entry
                    accumulatedOrders[accumulatedOrderIndex - 1].baseValuesPerFund[order.orderFundCode] = new Big(0);
                    accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund[order.orderFundCode] = order.orderValue;
                }
                // accumulate shares per fund
                if (accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode]) {
                    // add shares if existing
                    accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode] = accumulatedOrders[accumulatedOrderIndex].sharesPerFund[order.orderFundCode]
                            .plus(order.orderShares);
                } else {
                    // create new entry
                    accumulatedOrders[accumulatedOrderIndex - 1].sharesPerFund[order.orderFundCode] = new Big(0);
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
                    orderDateObj : currentDate,
                    baseValue : accumulatedOrders[accumulatedOrderIndex].baseValue,
                    baseValuesPerFund : accumulatedOrders[accumulatedOrderIndex].baseValuesPerFund,
                    sharesPerFund : accumulatedOrders[accumulatedOrderIndex].sharesPerFund,
                });
    }

    // convert to numbers
    accumulatedOrders
            .forEach(function(accumulatedOrder) {
                accumulatedOrder.baseValue = Number.parseFloat(accumulatedOrder.baseValue.toFixed(2));
                Object.keys(accumulatedOrder.baseValuesPerFund)
                        .forEach(function(key) {
                            accumulatedOrder.baseValuesPerFund[key] = Number.parseFloat(accumulatedOrder.baseValuesPerFund[key].toFixed(20));
                        });
                Object.keys(accumulatedOrder.sharesPerFund)
                        .forEach(function(key) {
                            accumulatedOrder.sharesPerFund[key] = Number.parseFloat(accumulatedOrder.sharesPerFund[key].toFixed(20));
                        });
            });

    return accumulatedOrders;
}
