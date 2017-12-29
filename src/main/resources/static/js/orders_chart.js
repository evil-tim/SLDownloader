google.charts.load('current', {
    'packages' : [ 'annotationchart', 'corechart' ]
});
google.charts.setOnLoadCallback(initOrdersChart);

$(document).ready(function() {
    addOrderUpdateCallback(updateOrdersChart);
    addOrderUpdateCallback(updateOrdersSharesChart);
});

function initOrdersChart() {
    updateOrdersChart(getOrders());
    updateOrdersSharesChart(getOrders());
}

function updateOrdersChart(rawOrderData) {
    var chartData = new google.visualization.DataTable();
    chartData.addColumn('date', 'Date');
    chartData.addColumn('number', 'Base Value');

    buildOrders(
            rawOrderData,
            function(orderData) {
                orderData.forEach(function(order) {
                    order.orderDateObj = new Date(order.orderDate);
                });
                orderData.sort(function(a, b) {
                    return a.orderDateObj < b.orderDateObj ? -1
                            : (a.orderDateObj > b.orderDateObj ? 1 : 0);
                });
                var accumulatedOrderData = [];
                var accumulatedOrderIndex = -1;
                for (var i = 0; i < orderData.length; i++) {
                    if (accumulatedOrderData.length == 0
                            || accumulatedOrderData[accumulatedOrderIndex].orderDateObj < orderData[i].orderDateObj) {
                        var prevDate = new Date(orderData[i].orderDateObj);
                        prevDate.setDate(prevDate.getDate() - 1);
                        accumulatedOrderData
                                .push({
                                    orderDateObj : prevDate,
                                    orderValue : accumulatedOrderIndex >= 0 ? accumulatedOrderData[accumulatedOrderIndex].orderValue
                                            : 0,
                                });
                        accumulatedOrderIndex++;
                        accumulatedOrderData
                                .push({
                                    orderDateObj : orderData[i].orderDateObj,
                                    orderValue : orderData[i].orderValue
                                            + accumulatedOrderData[accumulatedOrderIndex].orderValue,
                                });
                        accumulatedOrderIndex++;
                    } else if (accumulatedOrderData[accumulatedOrderIndex].orderDateObj
                            .getTime() === orderData[i].orderDateObj.getTime()) {
                        accumulatedOrderData[accumulatedOrderIndex].orderValue = accumulatedOrderData[accumulatedOrderIndex].orderValue
                                + orderData[i].orderValue;
                    }
                }

                var currentDate = new Date();
                currentDate.setHours(0, 0, 0, 0);
                accumulatedOrderData
                        .push({
                            orderDateObj : currentDate,
                            orderValue : accumulatedOrderIndex >= 0 ? accumulatedOrderData[accumulatedOrderIndex].orderValue
                                    : 0,
                        });

                for (var i = 0; i < accumulatedOrderData.length; i++) {
                    chartData.addRow([ accumulatedOrderData[i].orderDateObj,
                            accumulatedOrderData[i].orderValue ]);
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

    buildOrders(
            rawOrderData,
            function(orderData) {
                orderData.forEach(function(order) {
                    order.orderDateObj = new Date(order.orderDate);
                });
                orderData.sort(function(a, b) {
                    return a.orderDateObj < b.orderDateObj ? -1
                            : (a.orderDateObj > b.orderDateObj ? 1 : 0);
                });

                var accumulatedOrderData = [];
                var accumulatedOrderIndex = -1;
                for (var i = 0; i < orderData.length; i++) {
                    if (accumulatedOrderData.length == 0
                            || accumulatedOrderData[accumulatedOrderIndex].orderDateObj < orderData[i].orderDateObj) {
                        var prevDate = new Date(orderData[i].orderDateObj);
                        prevDate.setDate(prevDate.getDate() - 1);
                        accumulatedOrderData.push({
                            orderDateObj : prevDate,
                        });
                        accumulatedOrderIndex++;
                        if (!accumulatedOrderData[accumulatedOrderIndex - 1]) {
                            uniqueFundCodes
                                    .forEach(function(fundCode) {
                                        accumulatedOrderData[accumulatedOrderIndex][fundCode] = undefined;
                                    });
                        } else {
                            uniqueFundCodes
                                    .forEach(function(fundCode) {
                                        accumulatedOrderData[accumulatedOrderIndex][fundCode] = accumulatedOrderData[accumulatedOrderIndex - 1][fundCode];
                                    });
                        }

                        accumulatedOrderData.push({
                            orderDateObj : orderData[i].orderDateObj,
                        });
                        accumulatedOrderIndex++;
                        uniqueFundCodes
                                .forEach(function(fundCode) {
                                    accumulatedOrderData[accumulatedOrderIndex][fundCode] = accumulatedOrderData[accumulatedOrderIndex - 1][fundCode];
                                });

                    }

                    if (!accumulatedOrderData[accumulatedOrderIndex][orderData[i].orderFundCode]) {
                        accumulatedOrderData[accumulatedOrderIndex - 1][orderData[i].orderFundCode] = 0;
                        accumulatedOrderData[accumulatedOrderIndex][orderData[i].orderFundCode] = 0;
                    }

                    accumulatedOrderData[accumulatedOrderIndex][orderData[i].orderFundCode] = accumulatedOrderData[accumulatedOrderIndex][orderData[i].orderFundCode]
                            + orderData[i].orderShares;
                }

                var currentDate = new Date();
                currentDate.setHours(0, 0, 0, 0);
                accumulatedOrderData.push({
                    orderDateObj : currentDate,
                });
                accumulatedOrderIndex++;
                if (!accumulatedOrderData[accumulatedOrderIndex - 1]) {
                    uniqueFundCodes
                            .forEach(function(fundCode) {
                                accumulatedOrderData[accumulatedOrderIndex][fundCode] = 0;
                            });
                } else {
                    uniqueFundCodes
                            .forEach(function(fundCode) {
                                accumulatedOrderData[accumulatedOrderIndex][fundCode] = accumulatedOrderData[accumulatedOrderIndex - 1][fundCode];
                            });
                }

                accumulatedOrderData.forEach(function(accumulatedOrder) {
                    var row = [ accumulatedOrder.orderDateObj ];
                    uniqueFundCodes.forEach(function(fundCode) {
                        row.push(accumulatedOrder[fundCode]);
                    });
                    chartData.addRow(row);
                });

                drawSharesChart(chartData);
            });
}

function drawSharesChart(chartData) {
    var chart = new google.visualization.AreaChart(document
            .getElementById('ordersSharesChart'));
    var options = {
        isStacked : true
    };
    chart.draw(chartData, options);
}