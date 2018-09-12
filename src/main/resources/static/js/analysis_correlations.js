$(document).ready(function() {
    initCorrelationMatrix();
});

function initCorrelationMatrix() {
    var dates = makeDateParameters();
    var ajaxCalls = makeAjaxRequests(dates);
    fetchCorrelationData(dates, ajaxCalls);
}

function makeDateParameters() {
    var currentDate = new Date();

    var yearToDate = new Date();
    yearToDate.setDate(currentDate.getDate() - 365);
    yearToDate.setMinutes(yearToDate.getMinutes()
            - currentDate.getTimezoneOffset());

    var monthToDate = new Date();
    monthToDate.setDate(currentDate.getDate() - 30);
    monthToDate.setMinutes(monthToDate.getMinutes()
            - currentDate.getTimezoneOffset());

    var minDates = [];
    minDates.push([ "All", "" ]);
    minDates.push([ "Year", yearToDate.toISOString().slice(0, 10) ]);
    minDates.push([ "Month", monthToDate.toISOString().slice(0, 10) ]);

    return minDates;
}

function makeAjaxRequests(dateParams) {
    var ajaxRequests = [];

    dateParams.forEach(function(date) {
        ajaxRequests.push($.ajax({
            url : "/api/navps/correlations",
            data : {
                dateFrom : date[1],
            }
        }));
    });

    return ajaxRequests;
}

function fetchCorrelationData(dates, ajaxCalls) {
    $.when.apply($, ajaxCalls).then(function(...correlationResults) {
        if (correlationResults.length == 1) {
            correlationResults = [ correlationResults ];
        }

        var results = [];
        correlationResults.forEach(function(correlationResult) {
            results.push(correlationResult[0]);
        });

        $.ajax({
            url : "/api/funds"
        }).done(function(funds) {
            buildCorrelationMatrixTable(funds, dates, results);
        });

    });
}

function buildCorrelationMatrixTable(funds, dates, correlations) {
    var table = $("#correlation_matrix_table").append("<tbody />");
    table.append(makeTableHeaderRow(funds));
    funds.forEach(function(fund) {
        var rows = makeTableDataRow(fund, funds, dates, correlations);
        rows.forEach(function(row) {
            table.append(row);
        });
    });
}

function makeTableHeaderRow(funds) {
    var row = $("<tr />");
    row.append($("<td />").attr("colspan", "2").addClass("empty"));
    funds.forEach(function(fund) {
        var cell = $("<td />").addClass("top-title").text(fund.name);
        row.append(cell);
    });
    return row;
}

function makeTableDataRow(fundRow, funds, dates, correlations) {
    var rows = [];
    dates.forEach(function(date, index) {
        var row = $("<tr />");
        if (index == 0) {
            row.append($("<td />").attr("rowspan", dates.length).addClass(
                    "side-title").text(fundRow.name));
        }
        row.append($("<td />").text(date[0]));
        funds.forEach(function(fundCol) {
            if (fundCol.code === fundRow.code) {
                if (index == 0) {
                    row.append($("<td />").attr("rowspan", dates.length)
                            .addClass("empty").addClass("empty-dark"));
                }
            } else {
                var cell = $("<td />");
                var value = correlations[index][fundRow.code][fundCol.code];
                if (value !== null) {
                    cell.addClass("data").addClass(getColorClass(value)).text(
                            value);
                } else {
                    cell.addClass("empty");
                }
                row.append(cell);
            }
        });
        rows.push(row);
    });

    return rows;
}

function getColorClass(value) {
    if (value) {
        if (value >= 0.9) {
            return "color-p09";
        } else if (value >= 0.8) {
            return "color-p08";
        } else if (value >= 0.7) {
            return "color-p07";
        } else if (value >= 0.6) {
            return "color-p06";
        } else if (value >= 0.5) {
            return "color-p05";
        } else if (value >= 0.4) {
            return "color-p04";
        } else if (value >= 0.3) {
            return "color-p03";
        } else if (value >= 0.2) {
            return "color-p02";
        } else if (value >= 0.1) {
            return "color-p01";
        } else if (value >= 0.0) {
            return "color-p00";
        } else if (value >= -0.1) {
            return "color-n01";
        } else if (value >= -0.2) {
            return "color-n02";
        } else if (value >= -0.3) {
            return "color-n03";
        } else if (value >= -0.4) {
            return "color-n04";
        } else if (value >= -0.5) {
            return "color-n05";
        } else if (value >= -0.6) {
            return "color-n06";
        } else if (value >= -0.7) {
            return "color-n07";
        } else if (value >= -0.8) {
            return "color-n08";
        } else if (value >= -0.9) {
            return "color-n09";
        } else {
            return "color-n10";
        }
    }
    return "";
}