$(document).ready(function() {
    initAnalysisFundPicker();
});

function initAnalysisFundPicker() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAnalysisFundPicker);
}

function updateAnalysisFundPicker(data) {
    $("#fundPicker").append(
            $("<option></option>").attr("value", null).attr("disabled",
                    "disabled").text("Nothing Selected"));
    for (var i = 0; i < data.length; i++) {
        $("#fundPicker").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#fundPicker').selectpicker('val', null);
    $('#fundPicker').selectpicker('refresh');
    $('#fundPicker').on('hidden.bs.select', updateChartFromPicker);
}

function updateChartFromPicker() {
    var fundCode = $('#fundPicker').val();
    if (fundCode) {
        disableFundPicker();
        updateChart({
            code : fundCode,
            name : $('#fundPicker > option[value="' + fundCode + '"]').text()
        });
    }
}

function disableFundPicker() {
    $('#fundPicker').prop('disabled', true);
    $('#fundPicker').selectpicker('refresh');
}
function enableFundPicker() {
    $('#fundPicker').prop('disabled', false);
    $('#fundPicker').selectpicker('refresh');
}

google.charts.load('current', {
    'packages' : [ 'annotationchart' ]
});
google.charts.setOnLoadCallback(updateChart);

function updateChart(fund) {
    var data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Value');
    if (fund) {
        $.ajax({
            url : "/api/navps/all",
            data : {
                fund : fund.code
            }
        }).then(function(result) {
            buildDataRows(result, data)
            drawChart(data);
            enableFundPicker();
        });
    } else {
        data.addRows([ [ new Date(), 0 ] ]);
        drawChart(data);
        enableFundPicker();
    }
}

function buildDataRows(result, data) {
    var rows = [];
    for (var i = 0; i < result.length; i++) {
        rows.push([ new Date(result[i].date), result[i].value ]);
    }
    data.addRows(rows);
}

function drawChart(data) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('chart_div'));
    var options = {
        displayAnnotations : false
    };
    chart.draw(data, options);
}