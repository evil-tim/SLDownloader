$(document).ready(function() {
    initAddOrderFundField();
    initAddOrderDateField();
    initAllOrdersTable();
    initAllOrdersCards();
    addOrderUpdateCallback(refreshAllOrdersTable);
    addOrderUpdateCallback(refreshAllOrdersCards);
});

function initAddOrderFundField() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAddOrderFundField);
}

function updateAddOrderFundField(data) {
    for (var i = 0; i < data.length; i++) {
        $("#orderFund").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#orderFund').selectpicker('refresh');
}

function initAddOrderDateField() {
    $('#orderDate').datepicker({
        container : "body",
        autoclose : true,
        clearBtn : true,
        format : "yyyy-mm-dd",
        endDate : new Date(),
    });
}

function addNewOrderAction() {
    if (!$('#orderDateField')[0].checkValidity()
            || !$('#orderFund')[0].checkValidity()
            || !$('#orderShares')[0].checkValidity()
            || !$('#orderValue')[0].checkValidity()) {
        $('#addOrderFormSubmit')[0].click();
        return;
    }

    addNewOrderEntry($('#orderDate').datepicker('getDate'), $('#orderFund')
            .val(), $("#orderFund option:selected").text(), $('#orderShares')
            .val(), $('#orderValue').val());

    $('#addOrderModal').modal('hide');

    $('#orderDate').datepicker('clearDates');
    $("#orderFund").val('');
    $("#orderFund").selectpicker("refresh");
    $('#orderShares').val('');
    $('#orderValue').val('');
}

function addNewOrderEntry(orderDate, orderFundCode, orderFundName, orderShares,
        orderValue) {
    if (orderDate) {
        var orderDateAdjusted = new Date(orderDate);
        orderDateAdjusted.setMinutes(orderDate.getMinutes()
                - orderDate.getTimezoneOffset());
        orderDate = orderDateAdjusted;
    }
    addOrder(orderDate.toISOString().slice(0, 10), orderFundCode,
            orderFundName, orderShares, orderValue);
}

function removeOrderEntry(id) {
    removeOrder(id);
}

function clearAllOrders() {
    confirm("Are you sure you want to clear all orders?") && clearOrders();
}

function loadOrdersAction() {
    if (!$('#ordersImportFile')[0].checkValidity()) {
        $('#importOrdersFormSubmit')[0].click();
        return;
    }

    var importFile = $('#ordersImportFile')[0].files[0];
    var reader = new FileReader();
    reader.onload = function(event) {
        var orders = null;
        try {
            orders = JSON.parse(event.target.result);
        } catch (e) {
        }
        (orders && importOrders(orders)) ? $('#loadOrdersModal').modal('hide')
                : alert("Invalid orders. Please check imported file.");
    };
    reader.readAsText(importFile);
}

function buildExportOrderFile(link) {
    $(link).attr(
            "href",
            "data:application/json;charset=UTF-8,"
                    + encodeURIComponent(JSON.stringify(getOrders())));
}

var allOrdersTable;

function initAllOrdersTable() {
    getOrdersWithCurrentValues(function(initialOrders) {
        allOrdersTable = $('#ordersTable')
                .dataTable(
                        {
                            processing : false,
                            serverSide : false,
                            paging : false,
                            info : false,
                            data : initialOrders,
                            columns : [
                                    {
                                        name : "date",
                                        data : "orderDate",
                                        orderable : false
                                    },
                                    {
                                        name : "fund",
                                        data : "orderFundName",
                                        orderable : false
                                    },
                                    {
                                        name : "shares",
                                        data : "orderShares",
                                        orderable : false,
                                        className : "text-right",
                                    },
                                    {
                                        name : "baseValue",
                                        data : "orderValue",
                                        orderable : false,
                                        className : "text-right",
                                        render : function(data, type, row, meta) {
                                            return data.formatCurrency();
                                        }
                                    },
                                    {
                                        name : "currentValue",
                                        data : "currentValue",
                                        orderable : false,
                                        className : "text-right",
                                        render : function(data, type, row, meta) {
                                            return data.formatCurrency();
                                        }
                                    },
                                    {
                                        name : "actions",
                                        orderable : false,
                                        render : function(data, type, row, meta) {
                                            return "<a class=\"btn btn-default btn-sm delete-order-btn\" "
                                                    + "style=\"padding-top: 1px; padding-bottom: 1px\" "
                                                    + "href=\"javascript:void(0)\" onclick=\"javascript:removeOrderEntry("
                                                    + row.id + ")\">Remove</a>";
                                        }
                                    } ],
                            searching : false,
                            lengthChange : false,
                            searchCols : [ null, null, null, null, null ],
                            order : [ [ 0, "desc" ], [ 1, "asc" ] ],
                            language : {
                                zeroRecords : "No Entries"
                            },
                        });
    });
}

function refreshAllOrdersTable(rawData) {
    getOrdersWithCurrentValues(function(data) {
        allOrdersTable.fnClearTable();
        if (data && data.length > 0) {
            allOrdersTable.fnAddData(data);
        }
    }, rawData);
}

function initAllOrdersCards() {
    refreshAllOrdersCards(getOrders());
}

function refreshAllOrdersCards(rawData) {
    getOrdersWithCurrentValues(function(data) {
        var orderSummaries = makeSummaries(data);
        $("#orderTotalCards").empty();
        orderSummaries.forEach(function(summary, index) {
            $("#orderTotalCards").append(buildOrderSummaryCard(summary));
            if (index % 4 === 3) {
                $("#orderTotalCards").append($("<div />", {
                    'class' : 'clearfix'
                }));
            }
        });
    }, rawData);
}

function makeSummaries(data) {
    var funds = [];
    var summaries = {};
    var totalSummary = {
        title : "Total",
        shares : 0,
        baseValue : 0,
        currentValue : 0,
    };

    data
            .forEach(function(order) {
                totalSummary.baseValue = totalSummary.baseValue
                        + order.orderValue;
                totalSummary.currentValue = totalSummary.currentValue
                        + order.currentValue;

                if (summaries[order.orderFundCode]) {
                    summaries[order.orderFundCode].shares = summaries[order.orderFundCode].shares
                            + order.orderShares;
                    summaries[order.orderFundCode].baseValue = summaries[order.orderFundCode].baseValue
                            + order.orderValue;
                    summaries[order.orderFundCode].currentValue = summaries[order.orderFundCode].currentValue
                            + order.currentValue;
                } else {
                    funds.push(order.orderFundCode);
                    summaries[order.orderFundCode] = {
                        title : order.orderFundName,
                        shares : order.orderShares,
                        baseValue : order.orderValue,
                        currentValue : order.currentValue,
                    }
                }

            });

    var summaryList = [];
    if (data.length > 0) {
        summaryList[0] = totalSummary;
    }

    funds.sort();
    funds.forEach(function(fundCode) {
        summaryList.push(summaries[fundCode]);
    })

    return summaryList;
}

function buildOrderSummaryCard(ordersSummary) {
    var panelHead = $("<div />", {
        "class" : "panel-heading"
    });
    panelHead.append(ordersSummary.title == "Total" ? $("<h4>Total</h4>")
            : $("<b>" + ordersSummary.title + "</b>"));
    var totalBaseValue = $("<tr />");
    totalBaseValue
            .append($("<td><b>Base Value :</b></td><td class='pull-right'>"
                    + ordersSummary.baseValue.formatCurrency() + "</td>"));
    var currentValue = $("<tr />");
    currentValue
            .append($("<td><b>Current Value :</b></td><td class='pull-right'>"
                    + ordersSummary.currentValue.formatCurrency() + "</td>"));
    var totalShares = $("<tr />");
    totalShares.append($("<td><b>Shares :</b></td><td class='pull-right'>"
            + ordersSummary.shares + "</td>"));
    var panelBodyTable = $("<table />")
    panelBodyTable.append(totalBaseValue);
    panelBodyTable.append(currentValue);
    if (ordersSummary.title != "Total") {
        panelBodyTable.append(totalShares);
    }
    var panelBody = $("<div />", {
        "class" : "panel-body"
    });
    panelBody.append(panelBodyTable);
    var panel = $("<div />", {
        "class" : "panel panel-default"
    });
    panel.append(panelHead);
    panel.append(panelBody);
    var baseContainer = $("<div />", {
        "class" : "col-sm-3 col-md-3 col-lg-3 col-xl-3 summary-card"
    });
    baseContainer.append(panel);
    return baseContainer;
}

Number.prototype.formatCurrency = function() {
    return this.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
};