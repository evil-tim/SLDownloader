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
    return JSON.parse(localStorage.getItem("orders"));
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
        orderShares : parseInt(orderShares),
        orderValue : parseInt(orderValue),
    });
    saveOrders(allOrders);
    executeCallbacks(allOrders);
}

function importOrders(orders) {
    if (!orders || !Array.isArray(orders) || orders.length <= 0) {
        return false;
    }
    for (var i = 0; i < orders.length; i++) {
        if (!orders[i].id || !orders[i].orderDate || !orders[i].orderFundCode
                || !orders[i].orderFundName || !orders[i].orderShares
                || !Number.isInteger(orders[i].orderShares)
                || !orders[i].orderValue
                || !Number.isInteger(orders[i].orderValue)) {
            return false;
        }
    }
    saveOrders(orders);
    executeCallbacks(orders);
    return true;
}

function removeOrder(id) {
    var allOrders = getOrders();
    var orderIndex = -1;
    for (var i = 0; i < allOrders.length; i += 1) {
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