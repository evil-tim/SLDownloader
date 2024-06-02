$(document).ready(function() {
    initAnalysisFundPicker();
    initAnalysisPrecictionTypePicker();
});

function initAnalysisFundPicker() {
    $.ajax({
        url : '/api/funds'
    }).done(updateAnalysisFundPicker);
}

function updateAnalysisFundPicker(data) {
    $('#fundPicker').append(
            $('<option></option>').attr('value', null).attr('disabled',
                    'disabled').text('Nothing Selected'));
    for (let i = 0; i < data.length; i++) {
        $('#fundPicker').append(
                $('<option></option>').attr('value', data[i].code).text(
                        data[i].name));
    }
    $('#fundPicker').selectpicker('val', null);
    $('#fundPicker').selectpicker('refresh');
    $('#fundPicker').on('hidden.bs.select', updateChartFromPicker);
}

function initAnalysisPrecictionTypePicker() {
    $.ajax({
        url : '/api/navps/predictions/types'
    }).done(updateAnalysisPrecictionTypePicker);
}

function updateAnalysisPrecictionTypePicker(data) {
    $('#predTypePicker').append(
            $('<option></option>').attr('value', null).attr('disabled',
                    'disabled').text('Nothing Selected'));
    for (let i = 0; i < data.length; i++) {
        $('#predTypePicker').append(
                $('<option></option>').attr('value', data[i]).text(data[i]));
    }
    $('#predTypePicker').selectpicker('val', null);
    $('#predTypePicker').selectpicker('refresh');
    $('#predTypePicker').on('hidden.bs.select', updateChartFromPicker);
}

function updateChartFromPicker() {
    var fundCode = $('#fundPicker').val();
    var predType = $('#predTypePicker').val();
    if (fundCode && predType) {
        disableControls();
        updateChart({
            code : fundCode,
            type : predType
        });
    }
}

var controlsDisabled = true;

function disableControls() {
    controlsDisabled = true;
    $('#fundPicker').prop('disabled', true);
    $('#predTypePicker').prop('disabled', true);
    $('#fundPicker').selectpicker('refresh');
    $('#predTypePicker').selectpicker('refresh');
}

function enableControls() {
    controlsDisabled = false;
    $('#fundPicker').prop('disabled', false);
    $('#predTypePicker').prop('disabled', false);
    $('#fundPicker').selectpicker('refresh');
    $('#predTypePicker').selectpicker('refresh');
}

google.charts.load('current', {
    'packages' : [ 'annotationchart' ]
});
google.charts.setOnLoadCallback(updateChart);

function updateChart(selectedData) {
    var data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'NAVPS');

    if (selectedData) {
        var currentDate = new Date();

        var currYearFrom = new Date();
        currYearFrom.setDate(currentDate.getDate() - 365);
        currYearFrom.setMinutes(currYearFrom.getMinutes()
                - currentDate.getTimezoneOffset());

        var deferreds = [
            $.ajax({
                url : '/api/navps/all',
                data : {
                    fund : selectedData.code
                }
            }),
             $.ajax({
                url : '/api/navps/predictions',
                data : {
                    type : selectedData.type,
                    fund : selectedData.code,
                    dateFrom : currYearFrom.toISOString().slice(0, 10),
                    dateTo : currentDate.toISOString().slice(0, 10)
                }
            })
        ];

        $.when.apply($, deferreds).then(function(...results) {
            var navpsData = results[0][0];
            var predData = results[1][0];
            buildDataRows(navpsData, predData, data);
            drawChart(data, Object.keys(predData).length);
            enableControls();
        });

    } else {
        var date1 = new Date();
        date1.setHours(0, 0, 0, 0);
        date1.setDate(date1.getDate() - 1);
        var date2 = new Date();
        date2.setHours(0, 0, 0, 0);
        var tempRow1 = [ date1, 0 ];
        var tempRow2 = [ date2, 0 ];
        data.addRows([ tempRow1, tempRow2 ]);
        drawChart(data, 0);
        enableControls();
    }
}

function buildDataRows(navpsData, predData, data) {
    var numPredictions = Object.keys(predData).length;

    // date rage of navps
    var lastNavpsDate = new Date(navpsData[0].date);
    lastNavpsDate.setDate(lastNavpsDate.getDate() - lastNavpsDate.getDay());
    var firstNavpsDate = new Date(navpsData[navpsData.length - 1].date);
    firstNavpsDate.setDate(firstNavpsDate.getDate() - firstNavpsDate.getDay());

    // date range of predictions
    var lastPredictionDate = null;
    Object.keys(predData).forEach(function(predDateStr) {
        var predDate = new Date(predDateStr);
        if (lastPredictionDate == null
            || predDate > lastPredictionDate) {
            lastPredictionDate = predDate;
        }
    });
    if (lastPredictionDate != null) {
        lastPredictionDate.setDate(lastPredictionDate.getDate() - lastPredictionDate.getDay());
    }

    // overall date range
    var startDate = firstNavpsDate;
    var endDate = lastPredictionDate != null ? lastPredictionDate : lastNavpsDate;

    // setup columns
    var colIndex = 0;
    var predictionColumnIndexes = {};
    Object.keys(predData).forEach(function(predDateStr) {
        data.addColumn('number', 'Prediction ' + predDateStr);

        // build column index lookup
        predictionColumnIndexes[predDateStr] = colIndex + 2;
        colIndex++;
    })

    // setup rows
    var rowIndex = 0;
    var startOfWeek = new Date(startDate.getTime());
    var rowIndexes = {};
    while (startOfWeek <= endDate) {
        for (var i = 1; i <= 5; i++) {
            var weekDay = new Date(startOfWeek.getTime());
            weekDay.setDate(weekDay.getDate() + i);
            row = [weekDay, null];
            for (var j = 0; j < numPredictions; j++) {
                row.push(null);
            }
            data.addRow(row);

            // build row index lookup
            var weekDayStr =
                weekDay.getFullYear()
                + "-" + ("0" + (weekDay.getMonth() + 1)).slice(-2)
                + "-" + ("0" + weekDay.getDate()).slice(-2);
            rowIndexes[weekDayStr] = rowIndex;
            rowIndex++;
        }
        startOfWeek.setDate(startOfWeek.getDate() + 7);
    }

    // add navps
    for (var i = 0; i < navpsData.length; i++) {
        var navpsDate = navpsData[i].date;
        var rowIndex = rowIndexes[navpsDate];
        if (rowIndex !== undefined) {
            data.setCell(rowIndex, 1, navpsData[i].value)
        }
    }

    // add predictions
    Object.keys(predData).forEach(function(predDateStr) {
        var colIndex = predictionColumnIndexes[predDateStr];
        predData[predDateStr].forEach(function(prediction) {
            var predWeekDayDate = new Date(predDateStr);
            predWeekDayDate.setDate(predWeekDayDate.getDate() + prediction.daysInAdvance);
            var predWeekDayDateStr =
                predWeekDayDate.getFullYear()
                + "-" + ("0" + (predWeekDayDate.getMonth() + 1)).slice(-2)
                + "-" + ("0" + predWeekDayDate.getDate()).slice(-2);
            var rowIndex = rowIndexes[predWeekDayDateStr];
            if (colIndex !== undefined && rowIndex !== undefined) {
                data.setCell(rowIndex, colIndex, prediction.value)
            }
        });
    });
}

function drawChart(data, predColumnCount) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('prediction_chart'));

    var options = {
        displayAnnotations : false,
        colors : [ '#2471a3' ]
    };

    for (let i = 0; i < predColumnCount; i++) {
        options.colors.push('#7fb3d5')
    }

    chart.draw(data, options);
}