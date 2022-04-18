google.charts.load('current', {
    'packages' : [ 'annotationchart' ]
});
google.charts.setOnLoadCallback(updateChart);

function updateChart(fundList) {
    var data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    if (fundList && fundList.length > 0) {
        for (let i = 0; i < fundList.length; i++) {
            data.addColumn('number', fundList[i].name);
        }
        var deferreds = [];
        for (let i = 0; i < fundList.length; i++) {
            deferreds.push(getNAVPSDataDeferred(fundList[i].code));
        }
        $.when.apply($, deferreds).then(function() {
            buildDataRows(fundList.length, arguments, data);
            drawChart(data);
            enableFundPicker();
        });
    } else {
        var date1 = new Date();
        date1.setHours(0, 0, 0, 0);
        date1.setDate(date1.getDate() - 1);
        var date2 = new Date();
        date2.setHours(0, 0, 0, 0);
        data.addColumn('number', 'Value');
        data.addRows([ [ date1, 0 ], [ date2, 0 ] ]);
        drawChart(data);
        enableFundPicker();
    }
}

function drawChart(data) {
    var chart = new google.visualization.AnnotationChart(document
            .getElementById('chart_div'));
    var options = {
        displayAnnotations : false
    };
    chart.draw(data, options);
}

function getNAVPSDataDeferred(code) {
    return $.ajax({
        url : "/api/navps/all",
        data : {
            fund : code
        }
    });
}

function buildDataRows(count, arguments, data) {
    // fix arguments if only 1 result
    if (count === 1) {
        arguments = {
            0 : arguments
        };
    }

    var hasData = false;
    var fundCtrs = new Array(count).fill(0);
    var rows = [];
    // cycle through all result items
    do {
        // check if args still has data
        hasData = false;
        for (let i = 0; i < count; i++) {
            if (arguments[i] && arguments[i][0] && arguments[i][0][fundCtrs[i]]) {
                hasData = true;
            }
        }
        if (hasData) {
            // convert all dates of current entries
            var dateArr = [];
            for (let i = 0; i < count; i++) {
                if (arguments[i] && arguments[i][0]
                        && arguments[i][0][fundCtrs[i]]) {
                    arguments[i][0][fundCtrs[i]].date = new Date(
                            arguments[i][0][fundCtrs[i]].date);
                    dateArr.push(arguments[i][0][fundCtrs[i]].date);
                }
            }
            // get latest date
            var maxDate = new Date(Math.max.apply(null, dateArr));
            // build data table row
            var row = [];
            row.push(maxDate);
            for (let i = 0; i < count; i++) {
                if (arguments[i]
                        && arguments[i][0]
                        && arguments[i][0][fundCtrs[i]]
                        && arguments[i][0][fundCtrs[i]].date.getTime() === maxDate
                                .getTime()) {
                    // add entries that have the latest dates to the data table
                    row.push(arguments[i][0][fundCtrs[i]].value);
                    fundCtrs[i]++;
                } else {
                    row.push(null);
                }
            }
            rows.push(row);
        }
    } while (hasData);
    data.addRows(rows);
}