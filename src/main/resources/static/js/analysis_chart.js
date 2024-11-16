$(document).ready(function() {
    initAnalysisFundPicker();
    initAnalysisModulePicker();
    initAnalysisModulePickerExtras();
});

function initAnalysisFundPicker() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAnalysisFundPicker);
}

function initAnalysisModulePicker() {
    $(".analysis-selector").change(updateChartFromPicker);
}

function initAnalysisModulePickerExtras() {
    $(".analysis-card .selector").click(selectAllAnalysisModules);
}

function updateAnalysisFundPicker(data) {
    $("#fundPicker").append(
            $("<option></option>").attr("value", null).attr("disabled",
                    "disabled").text("Nothing Selected"));
    for (let i = 0; i < data.length; i++) {
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

var controlsDisabled = true;

function disableControls() {
    controlsDisabled = true;
    $(".analysis-selector").prop('disabled', true);
    $('#fundPicker').prop('disabled', true);
    $('#fundPicker').selectpicker('refresh');
}
function enableControls() {
    controlsDisabled = false;
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
        var date1 = new Date();
        date1.setHours(0, 0, 0, 0);
        date1.setDate(date1.getDate() - 1);
        var date2 = new Date();
        date2.setHours(0, 0, 0, 0);
        var tempRow1 = [ date1, 0 ];
        var tempRow2 = [ date2, 0 ];
        data.addRows([ tempRow1, tempRow2 ]);
        drawChart(data);
        enableControls();
    }
}

function buildDataRows(result, data) {
    var dates = [];
    var values = [];
    for (let i = 0; i < result.length; i++) {
        dates.push(new Date(result[i].entryDate));
        values.push(result[i].fundValue);
    }
    var rows = [];
    var moduleNames = analysisModules.getModules();
    var selectedModuleNames = getSelectedModules();

    moduleNames.filter(function(moduleName) {
        return selectedModuleNames.indexOf(moduleName) >= 0;
    }).forEach(function(moduleName) {
        analysisModules.init(moduleName);
    });

    for (let i = 0; i < result.length; i++) {
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

function selectAllAnalysisModules(element) {
    if (controlsDisabled) {
        return;
    }

    var selector = $(element.currentTarget);
    var checkboxes = selector.parent().parent().parent().parent().find(
            ".analysis-selector");
    var checkboxesToChange = null;

    if (selector.hasClass("selector-select")) {
        checkboxesToChange = checkboxes
                .filter(".analysis-selector:not(:checked)");
    } else if (selector.hasClass("selector-deselect")) {
        checkboxesToChange = checkboxes.filter(".analysis-selector:checked");
    }

    if (checkboxesToChange && checkboxesToChange.length > 0) {
        var status = checkboxesToChange.prop("checked");
        checkboxesToChange.prop("checked", !status);
        updateChartFromPicker();
    }
}