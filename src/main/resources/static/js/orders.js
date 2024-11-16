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
        rawOrder.orderValue = new Big(rawOrder.orderValue);
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
        orderValue : new Big(orderValue),
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
                || !orders[i].orderFundName || !orders[i].orderShares
                || !orders[i].orderValue) {
            return false;
        }
    }

    // convert fixed point string to number
    orders.forEach(function(order) {
        order.orderShares = new Big(order.orderShares);
        order.orderValue = new Big(order.orderValue);
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

    // fetch latest navps
    $.when
            .apply($, latestNavpsRequests)
            .then(
                    function(...navpsLookupResults) {
                        if (latestNavpsRequests.length == 1) {
                            navpsLookupResults = [ navpsLookupResults ];
                        }
                        // convert multiple navps lookups to single lookup
                        var currentNavps = {};
                        if (navpsLookupResults) {
                            navpsLookupResults
                                    .forEach(function(navpsLookupResult) {
                                        if (navpsLookupResult
                                                && navpsLookupResult[0]
                                                && navpsLookupResult[0].content
                                                && navpsLookupResult[0].content[0]
                                                && navpsLookupResult[0].content[0].fund
                                                && navpsLookupResult[0].content[0].fundValue) {
                                            currentNavps[navpsLookupResult[0].content[0].fund] = navpsLookupResult[0].content[0].fundValue;
                                        }
                                    });
                        }
                        // convert raw orders to orders with current values
                        var processedOrders = [];
                        rawOrders
                                .forEach(function(rawOrder) {
                                    var currentValue = currentNavps && currentNavps[rawOrder.orderFundCode]
                                            ? rawOrder.orderShares.times(currentNavps[rawOrder.orderFundCode])
                                            : 0;
                                    processedOrders
                                            .push({
                                                id : rawOrder.id,
                                                orderDate : rawOrder.orderDate,
                                                orderFundCode : rawOrder.orderFundCode,
                                                orderFundName : rawOrder.orderFundName,
                                                orderShares : rawOrder.orderShares,
                                                orderValue : rawOrder.orderValue,
                                                currentValue : currentValue,
                                            });
                                });
                        callback(processedOrders);
                    });
}