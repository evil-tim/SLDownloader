$(document).ready(function() {
    initScatterFundPicker();
});

function initScatterFundPicker() {
    $.ajax({
        url : "/api/funds"
    }).done(updateScatterFundPickers);
}

function updateScatterFundPickers(data) {
    updateScatterFundPicker($("#fundPickerX"), data);
    updateScatterFundPicker($("#fundPickerY"), data);
}

function updateScatterFundPicker(picker, data) {
    picker.append($("<option></option>").attr("value", null).attr("disabled",
            "disabled").text("Nothing Selected"));
    for (var i = 0; i < data.length; i++) {
        picker.append($("<option></option>").attr("value", data[i].code).text(
                data[i].name));
    }
    picker.selectpicker('val', null);
    picker.selectpicker('refresh');
    picker.on('hidden.bs.select', updateChartFromPicker);
}

function updateChartFromPicker(event) {
    $("#fundPickerX option").removeAttr('disabled');
    $("#fundPickerY option").removeAttr('disabled');
    $("#fundPickerX option:first").attr('disabled', 'disabled');
    $("#fundPickerY option:first").attr('disabled', 'disabled');

    var fundCodeX = $('#fundPickerX').val();
    var fundCodeY = $('#fundPickerY').val();

    if (fundCodeX) {
        $("#fundPickerY option[value=\"" + fundCodeX + "\"]").attr('disabled',
                'disabled');
        $('#fundPickerY').selectpicker('refresh');
    }

    if (fundCodeY) {
        $("#fundPickerX option[value=\"" + fundCodeY + "\"]").attr('disabled',
                'disabled');
        $('#fundPickerX').selectpicker('refresh');
    }

    if (fundCodeX && fundCodeY && fundCodeX != fundCodeY) {
        disableControls();
        updateChart({
            fundCodeX : fundCodeX,
            fundCodeY : fundCodeY,
        });
    }
}

google.charts.load('current', {
    'packages' : [ 'corechart' ]
});
google.charts.setOnLoadCallback(updateChart);

function updateChart(funds) {
    var data = new google.visualization.DataTable();
    data.addColumn('number', '');
    data.addColumn('number', 'Current Year');
    data.addColumn('number', 'Previous Year');

    if (funds) {
        var currentDate = new Date();

        var currYearFrom = new Date();
        currYearFrom.setDate(currentDate.getDate() - 365);
        currYearFrom.setMinutes(currYearFrom.getMinutes()
                - currentDate.getTimezoneOffset());

        var prevYearTo = new Date();
        prevYearTo.setDate(currentDate.getDate() - 366);
        prevYearTo.setMinutes(prevYearTo.getMinutes()
                - currentDate.getTimezoneOffset());

        var prevYearFrom = new Date();
        prevYearFrom.setDate(currentDate.getDate() - 730);
        prevYearFrom.setMinutes(prevYearFrom.getMinutes()
                - currentDate.getTimezoneOffset());

        var deferreds = [
                getScatterNAVPSDataDeferred(funds.fundCodeX, funds.fundCodeY,
                        currYearFrom.toISOString().slice(0, 10), currentDate
                                .toISOString().slice(0, 10)),
                getScatterNAVPSDataDeferred(funds.fundCodeX, funds.fundCodeY,
                        prevYearFrom.toISOString().slice(0, 10), prevYearTo
                                .toISOString().slice(0, 10)) ];

        $.when.apply($, deferreds).then(function() {
            buildDataRows(arguments[0][0], arguments[1][0], data);
            drawChart(data, {
                trendlines : {
                    0 : {
                        type : 'linear',
                        color : '#2962FF',
                        lineWidth : 1,
                        opacity : 0.5,
                        showR2 : true,
                        visibleInLegend : false
                    },
                    1 : {
                        type : 'linear',
                        color : '#82B1FF',
                        lineWidth : 1,
                        opacity : 0.5,
                        showR2 : true,
                        visibleInLegend : false
                    }
                }
            });
            enableControls();
        });
    } else {
        drawChart(data);
        enableControls();
    }
}

function buildDataRows(currYear, prevYear, data) {
    var rows = [];

    currYear.forEach(function(point) {
        rows.push([ point.first, point.second, null ]);
    });

    prevYear.forEach(function(point) {
        rows.push([ point.first, null, point.second ]);
    });

    data.addRows(rows)
}

function drawChart(data, additionalOptions) {
    var chartAll = new google.visualization.ScatterChart(document
            .getElementById('scatter_chart'));

    var options = {
        chartArea : {
            width : '100%',
            height : '100%'
        },
        titlePosition : 'in',
        legend : {
            position : 'in'
        },
        axisTitlesPosition : 'in',
        hAxis : {
            textPosition : 'in'
        },
        vAxis : {
            textPosition : 'in'
        },
        series : {
            0 : {
                color : '#2962FF',
                pointSize : 4
            },
            1 : {
                color : '#82B1FF',
                pointSize : 4
            }
        }
    };

    if (additionalOptions) {
        $.extend(options, additionalOptions);
    }

    chartAll.draw(data, options);
}

function disableControls() {
    $('#fundPickerX').prop('disabled', true);
    $('#fundPickerY').prop('disabled', true);
    $('#fundPickerX').selectpicker('refresh');
    $('#fundPickerY').selectpicker('refresh');
}

function enableControls() {
    $('#fundPickerX').prop('disabled', false);
    $('#fundPickerY').prop('disabled', false);
    $('#fundPickerX').selectpicker('refresh');
    $('#fundPickerY').selectpicker('refresh');
}

function getScatterNAVPSDataDeferred(codeX, codeY, dateFrom, dateTo) {
    return $.ajax({
        url : "/api/navps/scatter",
        data : {
            fundX : codeX,
            fundY : codeY,
            dateFrom : dateFrom,
            dateTo : dateTo
        }
    });
}