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

        $.ajax({
            url : '/api/navps/all',
            data : {
                fund : selectedData.code
            }
        }).then(function(navpsData) {
            $.ajax({
                url : '/api/navps/predictions',
                data : {
                    type : selectedData.type,
                    fund : selectedData.code,
                    dateFrom : currYearFrom.toISOString().slice(0, 10),
                    dateTo : currentDate.toISOString().slice(0, 10)
                }
            }).then(function(predData) {
                buildDataRows(navpsData, predData, data);
                drawChart(data, Object.keys(predData).length);
                enableControls();
            });
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
    var dates = [];
    var rows = [];
    var predictionDates = [];

    Object.keys(predData).forEach(function(predDateStr) {
        data.addColumn('number', 'Prediction ' + predDateStr);
        predictionDates.push([ new Date(predDateStr), predDateStr ]);
    })

    for (let i = 0; i < navpsData.length; i++) {
        var dateObj = new Date(navpsData[i].date);
        var value = navpsData[i].value;

        dates.push(dateObj);

        var row = [ dateObj, value ];
        predictionDates.forEach(function() {
            row.push(null);
        });

        rows.push(row);
    }

    data.addRows(rows);

    var additionalPredictionRows = [];
    predictionDates
            .forEach(function(predDate, predictionSeriesIndex) {
                var startIndex = -1;
                for (let i = 0; i < dates.length; i++) {
                    if (predDate[0] == dates[i]) {
                        startIndex = i;
                        break;
                    } else if (predDate[0] > dates[i]) {
                        startIndex = i - 1;
                        break;
                    }
                }

                predData[predDate[1]]
                        .forEach(function(prediction) {
                            var predValueIndex = startIndex
                                    - prediction.daysInAdvance + 1;

                            if (predValueIndex >= 0) {
                                data.setCell(predValueIndex,
                                        predictionSeriesIndex + 2,
                                        prediction.value);
                            } else {
                                var predictionDate = new Date(predDate[0]);
                                predictionDate.setDate(predictionDate.getDate()
                                        + prediction.daysInAdvance - 1);

                                var additionalPredictionRow = null;
                                for (let i = 0; i < additionalPredictionRows.length; i++) {
                                    if (additionalPredictionRows[i][0] == predictionDate) {
                                        additionalPredictionRow = additionalPredictionRows[i];
                                        break;
                                    }
                                }
                                if (!additionalPredictionRow) {
                                    additionalPredictionRow = [ predictionDate,
                                            null ];
                                    predictionDates.forEach(function() {
                                        additionalPredictionRow.push(null);
                                    });
                                    additionalPredictionRows
                                            .push(additionalPredictionRow);
                                }
                                additionalPredictionRow[predictionSeriesIndex + 2] = prediction.value;
                            }
                        });

            });

    data.addRows(additionalPredictionRows);
}

function drawChart(data, predColumnCount) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('prediction_chart'));

    var options = {
        displayAnnotations : false,
        colors : [ '#2471a3' ]
    };

    for (var i = 0; i < predColumnCount; i++) {
        options.colors.push('#7fb3d5')
    }

    chart.draw(data, options);
}