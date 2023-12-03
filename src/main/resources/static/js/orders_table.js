$(document).ready(function() {
    initAddOrderFundField();
    initAddOrderDateField();
    initAllOrdersTable();
    initAllOrdersCards();
    addOrderUpdateCallback(refreshAllOrdersTable);
    addOrderUpdateCallback(refreshAllOrdersCards);
    initEvents();
});

function initEvents() {
    $('#clearAllOrders').on('click', clearAllOrdersAction);
    $('#exportOrders').on('click', buildExportOrderFile);
    $('#loadOrders').on('click', loadOrdersAction);
    $('#addOrder').on('click', addNewOrderAction);
    $('#ordersTable').on('click', '.delete-order-btn', removeOrderAction)
}

function initAddOrderFundField() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAddOrderFundField);
}

function updateAddOrderFundField(data) {
    for (let i = 0; i < data.length; i++) {
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

function removeOrderAction(event) {
    removeOrder($(event.target).data("orderid"));
}

function clearAllOrdersAction() {
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

function buildExportOrderFile(event) {
    $(event.target).attr(
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
                                        render : function(data, _type, _row, _meta) {
                                            return formatBigToNumber(data);
                                        }
                                    },
                                    {
                                        name : "baseValue",
                                        data : "orderValue",
                                        orderable : false,
                                        className : "text-right",
                                        render : function(data, _type, _row, _meta) {
                                            return formatBigToCurrency(data);
                                        }
                                    },
                                    {
                                        name : "currentValue",
                                        data : "currentValue",
                                        orderable : false,
                                        className : "text-right",
                                        render : function(data, _type, _row, _meta) {
                                            return formatBigToCurrency(data);
                                        }
                                    },
                                    {
                                        name : "percentGain",
                                        orderable : false,
                                        className : "text-right",
                                        render : function(_data, _type, row, _meta) {
                                            return formatBigToPercent(
                                                        row.currentValue
                                                        .minus(row.orderValue)
                                                        .div(row.orderValue));
                                        }
                                    },
                                    {
                                        name : "actions",
                                        orderable : false,
                                        render : function(_data, _type, row, _meta) {
                                            return "<button class=\"btn btn-default btn-sm delete-order-btn\" "
                                                    + "style=\"padding-top: 1px; padding-bottom: 1px\" "
                                                    + "data-orderid=\"" + row.id + "\">Remove</button>";
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
        shares : new Big(0),
        baseValue : new Big(0),
        currentValue : new Big(0),
    };

    data
            .forEach(function(order) {
                totalSummary.baseValue = totalSummary.baseValue
                        .plus(order.orderValue);
                totalSummary.currentValue = totalSummary.currentValue
                        .plus(order.currentValue);

                if (summaries[order.orderFundCode]) {
                    summaries[order.orderFundCode].shares = summaries[order.orderFundCode].shares
                            .plus(order.orderShares);
                    summaries[order.orderFundCode].baseValue = summaries[order.orderFundCode].baseValue
                            .plus(order.orderValue);
                    summaries[order.orderFundCode].currentValue = summaries[order.orderFundCode].currentValue
                            .plus(order.currentValue);
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
    });
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
                    + formatBigToCurrency(ordersSummary.baseValue) + "</td>"));

    var currentValue = $("<tr />");
    currentValue
            .append($("<td><b>Current Value :</b></td><td class='pull-right'>"
                    + formatBigToCurrency(ordersSummary.currentValue) + "</td>"));

    var gain = $("<tr />");
    gain
            .append($("<td><b>Gain :</b></td><td class='pull-right'>"
                    + formatBigToCurrency(
                            ordersSummary.currentValue
                            .minus(ordersSummary.baseValue)) + "</td>"));

    var percentGain = $("<tr />");
    percentGain
            .append($("<td><b>Percent Gain :</b></td><td class='pull-right'>"
                    + formatBigToPercent(
                            ordersSummary.currentValue
                            .minus(ordersSummary.baseValue)
                            .div(ordersSummary.baseValue)) + "</td>"));

    var panelBodyTable = $("<table />")
    panelBodyTable.append(totalBaseValue);
    panelBodyTable.append(currentValue);
    panelBodyTable.append(gain);
    panelBodyTable.append(percentGain);
    if (ordersSummary.title != "Total") {
        var totalShares = $("<tr />");
        totalShares.append($("<td><b>Shares :</b></td><td class='pull-right'>"
                + formatBigToNumber(ordersSummary.shares) + "</td>"));
        panelBodyTable.append(totalShares);
    } else {
        panelBodyTable.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
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

function formatBigToNumber(value) {
    if (value) {
        return value.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
    }
    return "";
}

function formatBigToCurrency(value) {
    if (value) {
        return value.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
    }
    return "";
}

function formatBigToPercent(value) {
    if (value) {
        return value.times(100).toFixed(2) + "%";
    }
    return "";
}