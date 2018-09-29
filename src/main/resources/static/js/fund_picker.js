$(document).ready(function() {
    initFundPicker();
    initEvents();
});

function initEvents() {
    $('#fundPicker').on('hidden.bs.select', updateChartFromPicker);
    $('#fundChartRefresh').on('click', updateChartFromPicker);
}

function updateChartFromPicker() {
    disableFundPicker();
    var fundCodeArr = $('#fundPicker').val();
    var fundArr = [];
    for (var i = 0; i < fundCodeArr.length; i++) {
        fundArr.push({
            code : fundCodeArr[i],
            name : $('#fundPicker > option[value="' + fundCodeArr[i] + '"]')
                    .text()
        });
    }
    updateChart(fundArr);
}

function disableFundPicker() {
    $('#fundPicker').prop('disabled', true);
    $('#fundPicker').selectpicker('refresh');
}
function enableFundPicker() {
    $('#fundPicker').prop('disabled', false);
    $('#fundPicker').selectpicker('refresh');
}

function initFundPicker() {
    $.ajax({
        url : "/api/funds"
    }).done(updateFundPicker);
}

function updateFundPicker(data) {
    for (var i = 0; i < data.length; i++) {
        $("#fundPicker").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#fundPicker').selectpicker('refresh');
}