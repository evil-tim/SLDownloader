var callbacks = [];

function addOrderUpdateCallback(callback) {
    callbacks.push(callback);
}

function executeCallbacks(data) {
    callbacks.forEach(function(callback) {
        callback(data);
    });
}

function getOrders() {
    initOrders();
    var orders = JSON.parse(localStorage.getItem("orders"));

    // convert fixed point string to number
    orders.forEach(function(rawOrder) {
        rawOrder.orderShares = new Big(rawOrder.orderShares);
        rawOrder.orderValue = rawOrder.orderValue ? new Big(rawOrder.orderValue) : undefined;
    });

    return orders;
}

function saveOrders(orders) {
    localStorage.setItem("orders", JSON.stringify(orders));
}

function initOrders() {
    if (!localStorage.getItem("orders")) {
        saveOrders([]);
    }
}

function addOrder(orderDate, orderFundCode, orderFundName, orderShares,
        orderValue) {
    var allOrders = getOrders();
    var maxId = allOrders.length > 0 ? allOrders[allOrders.length - 1].id : 0;
    allOrders.push({
        id : maxId + 1,
        orderDate : orderDate,
        orderFundCode : orderFundCode,
        orderFundName : orderFundName,
        orderShares : new Big(orderShares),
        orderValue : orderValue ? new Big(orderValue) : undefined,
    });
    saveOrders(allOrders);
    executeCallbacks(allOrders);
}

function importOrders(orders) {
    if (!orders || !Array.isArray(orders) || orders.length <= 0) {
        return false;
    }
    for (let i = 0; i < orders.length; i++) {
        if (!orders[i].id || !orders[i].orderDate || !orders[i].orderFundCode
                || !orders[i].orderFundName || !orders[i].orderShares) {
            return false;
        }
        if (orders[i].orderShares && orders[i].orderShares >= 0 && !orders[i].orderValue) {
            return false;
        }
    }

    // convert fixed point string to number
    orders.forEach(function(order) {
        order.orderShares = new Big(order.orderShares);
        order.orderValue = order.orderValue ? new Big(order.orderValue) : undefined;
    });

    saveOrders(orders);
    executeCallbacks(orders);
    return true;
}

function removeOrder(id) {
    var allOrders = getOrders();
    var orderIndex = -1;
    for (let i = 0; i < allOrders.length; i += 1) {
        if (allOrders[i].id == id) {
            orderIndex = i;
            break;
        }
    }
    if (orderIndex != -1) {
        allOrders.splice(orderIndex, 1);
    }
    saveOrders(allOrders);
    executeCallbacks(allOrders);
}

function clearOrders() {
    saveOrders([]);
    executeCallbacks([]);
}

function getOrdersWithCurrentValues(callback, existingOrders) {
    // get or use provided orders
    var rawOrders = existingOrders ? existingOrders : getOrders();

    // get all unique fund codes in orders
    var allFundCodes = [];
    rawOrders.forEach(function(rawOrder) {
        if ($.inArray(rawOrder.orderFundCode, allFundCodes) == -1) {
            allFundCodes.push(rawOrder.orderFundCode);
        }
    });

    // get all fund codes + dates for all sell orders
    var fundCodesAndDates = [];
    rawOrders.forEach(function(rawOrder) {
        if (rawOrder.orderShares.lt(0)) {
            fundCodesAndDates.push({
                fundCode : rawOrder.orderFundCode,
                date : rawOrder.orderDate,
            });
        }
    });

    // build fetch latest navps requests
    var latestNavpsRequests = [];
    allFundCodes.forEach(function(fundCode) {
        latestNavpsRequests.push($.ajax({
            url : "/api/navps",
            data : {
                sort : "entryDate,desc",
                size : 1,
                fund : fundCode,
            }
        }));
    });

    // build fetch navps by date requests for sell orders
    var sellNavpsRequests = [];
    fundCodesAndDates.forEach(function(fundCodeAndDate) {
        sellNavpsRequests.push($.ajax({
            url : "/api/navps",
            data : {
                sort : "entryDate,desc",
                size : 1,
                fund : fundCodeAndDate.fundCode,
                dateFrom : fundCodeAndDate.date,
                dateTo : fundCodeAndDate.date,
            }
        }));
    });

    var latestNavpsStartIndex = 0;
    var latestNavpsEndIndex = latestNavpsRequests.length - 1;
    var sellNavpsStartIndex = latestNavpsEndIndex + 1;
    var sellNavpsEndIndex = latestNavpsEndIndex + sellNavpsRequests.length;
    var allRequests = [].concat(latestNavpsRequests, sellNavpsRequests);

    // fetch latest navps
    $.when
            .apply($, allRequests)
            .then(
                    function(...navpsLookupResults) {
                        if (allRequests.length == 1) {
                            navpsLookupResults = [ navpsLookupResults ];
                        }
                        // convert multiple navps lookups to single lookup
                        var currentNavps = {};
                        var sellNavps = {};
                        if (navpsLookupResults) {
                            for (var i = latestNavpsStartIndex; i <= latestNavpsEndIndex; i++) {
                                navpsLookupResult = navpsLookupResults[i];
                                if (!navpsLookupResult
                                    || !navpsLookupResult[0]
                                    || !navpsLookupResult[0].content
                                    || !navpsLookupResult[0].content[0]
                                    || !navpsLookupResult[0].content[0].fund
                                    || !navpsLookupResult[0].content[0].fundValue) {
                                    continue;
                                }
                                var currentFund = navpsLookupResult[0].content[0].fund;
                                var currentValue = navpsLookupResult[0].content[0].fundValue;
                                currentNavps[currentFund] = currentValue;
                            }
                            for (var i = sellNavpsStartIndex; i <= sellNavpsEndIndex; i++) {
                                navpsLookupResult = navpsLookupResults[i];
                                if (!navpsLookupResult
                                    || !navpsLookupResult[0]
                                    || !navpsLookupResult[0].content
                                    || !navpsLookupResult[0].content[0]
                                    || !navpsLookupResult[0].content[0].fund
                                    || !navpsLookupResult[0].content[0].fundValue
                                    || !navpsLookupResult[0].content[0].entryDate) {
                                    continue;
                                }
                                var currentFund = navpsLookupResult[0].content[0].fund;
                                var currentValue = navpsLookupResult[0].content[0].fundValue;
                                var entryDate = navpsLookupResult[0].content[0].entryDate;
                                if (!sellNavps[currentFund]) {
                                    sellNavps[currentFund] = {};
                                }
                                sellNavps[currentFund][entryDate] = currentValue;
                            }
                        }
                        // convert raw orders to orders with current values
                        var processedOrders = [];
                        // order by orderDate asc and compute current value
                        rawOrders
                                .sort(function(a, b) {
                                    var dateA = new Date(a.orderDate);
                                    var dateB = new Date(b.orderDate);
                                    return dateA - dateB;
                                })
                                .forEach(function(rawOrder) {
                                    var currentValue = currentNavps && currentNavps[rawOrder.orderFundCode]
                                            ? rawOrder.orderShares.times(currentNavps[rawOrder.orderFundCode])
                                            : Big(0);
                                    var realizedValue = undefined;
                                    if (rawOrder.orderShares.lt(0)) {
                                        var orderFundCode = rawOrder.orderFundCode;
                                        var orderDate = rawOrder.orderDate;
                                        if (sellNavps[orderFundCode] && sellNavps[orderFundCode][orderDate]) {
                                            realizedValue = rawOrder.orderShares.times(sellNavps[orderFundCode][orderDate]).times(-1);
                                        } else {
                                            realizedValue = new Big(0);
                                        }
                                    }
                                    processedOrders
                                            .push({
                                                id : rawOrder.id,
                                                orderDate : rawOrder.orderDate,
                                                orderFundCode : rawOrder.orderFundCode,
                                                orderFundName : rawOrder.orderFundName,
                                                orderShares : rawOrder.orderShares,
                                                orderValue : rawOrder.orderValue,
                                                currentValue : currentValue,
                                                realizedValue : realizedValue,
                                            });
                                });
                        callback(processedOrders);
                    });
}