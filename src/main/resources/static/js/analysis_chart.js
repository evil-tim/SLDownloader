$(document).ready(function() {
    initAnalysisFundPicker();
    initAnalysisModulePicker();
});

function initAnalysisFundPicker() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAnalysisFundPicker);
}

function initAnalysisModulePicker() {
    $(".analysis-selector").change(function() {
        updateChartFromPicker();
    });
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
        disableControls();
        updateChart({
            code : fundCode,
            name : $('#fundPicker > option[value="' + fundCode + '"]').text()
        });
    }
}

function disableControls() {
    $(".analysis-selector").prop('disabled', true);
    $('#fundPicker').prop('disabled', true);
    $('#fundPicker').selectpicker('refresh');
}
function enableControls() {
    $(".analysis-selector").prop('disabled', false);
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
    data.addColumn('number', 'NAVPS');

    var moduleNames = analysisModules.getModules();
    var selectedModuleNames = getSelectedModules();

    moduleNames.filter(function(moduleName) {
        return selectedModuleNames.indexOf(moduleName) >= 0;
    }).forEach(function(moduleName) {
        var moduleColumns = analysisModules.getColumns(moduleName);
        moduleColumns.forEach(function(moduleColumn) {
            data.addColumn(moduleColumn.type, moduleColumn.title);
        });
    });

    if (fund) {
        $.ajax({
            url : "/api/navps/all",
            data : {
                fund : fund.code
            }
        }).then(function(result) {
            buildDataRows(result, data)
            drawChart(data);
            enableControls();
        });
    } else {
        var tempRow = [ new Date(), 0 ];

        moduleNames.filter(function(moduleName) {
            return selectedModuleNames.indexOf(moduleName) >= 0;
        }).forEach(function(moduleName) {
            var moduleColumns = analysisModules.getColumns(moduleName);
            moduleColumns.forEach(function(moduleColumn) {
                tempRow.push(0);
            });
        });
        data.addRows([ tempRow ]);
        drawChart(data);
        enableControls();
    }
}

function buildDataRows(result, data) {
    var dates = [];
    var values = [];
    for (var i = 0; i < result.length; i++) {
        dates.push(new Date(result[i].date));
        values.push(result[i].value);
    }
    var rows = [];
    var moduleNames = analysisModules.getModules();
    var selectedModuleNames = getSelectedModules();

    moduleNames.filter(function(moduleName) {
        return selectedModuleNames.indexOf(moduleName) >= 0;
    }).forEach(function(moduleName) {
        analysisModules.init(moduleName);
    });

    for (var i = 0; i < result.length; i++) {
        var row = [ dates[i], values[i] ];

        moduleNames.filter(function(moduleName) {
            return selectedModuleNames.indexOf(moduleName) >= 0;
        }).forEach(function(moduleName) {
            var results = analysisModules.compute(moduleName, i, values);
            results.forEach(function(resultVal) {
                row.push(resultVal);
            });
        });

        rows.push(row);
    }
    data.addRows(rows);
}

function drawChart(data) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('chart_div'));

    var moduleNames = analysisModules.getModules();
    var selectedModuleNames = getSelectedModules();

    var options = {
        displayAnnotations : false,
        colors : [ '#667788' ]
    };

    moduleNames.filter(function(moduleName) {
        return selectedModuleNames.indexOf(moduleName) >= 0;
    }).forEach(function(moduleName) {
        var moduleColumns = analysisModules.getColumns(moduleName);
        moduleColumns.forEach(function(moduleColumn) {
            options.colors.push(moduleColumn.color);
        });
    });

    chart.draw(data, options);
}

function getSelectedModules() {
    return $(".analysis-selector:checked").map(function() {
        return this.id;
    }).get();
}