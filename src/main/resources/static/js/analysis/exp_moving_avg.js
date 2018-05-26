(function() {

    function movingAvgColumns(settings) {
        return [ {
            'type' : 'number',
            'title' : '' + settings.period + 'D EMA',
            'color' : settings.color
        } ];
    }

    function movingAvgCompute(index, data, settings) {
        var accum = 0;
        var valid = true;
        for (var i = 0; valid && i < settings.period; i++) {
            dataIndex = index + i;
            if (dataIndex < data.length) {
                accum += data[dataIndex] * settings.weights[i];
            } else {
                valid = false;
            }
        }
        return valid ? accum : null;
    }

    var movingAvg5Settings = {
        'period' : 5,
        'color' : '#DDC333',
        'weights' : [ 0.5 + 0.03125, 0.25, 0.125, 0.0625, 0.03125 ]
    };
    var movingAvg10Settings = {
        'period' : 10,
        'color' : '#DD5766',
        'weights' : [ 0.5 + 0.000976563, 0.25, 0.125, 0.0625, 0.03125,
                0.015625, 0.0078125, 0.00390625, 0.001953125, 0.000976563 ]
    };

    analysisModules.addModule('ema_5', movingAvgColumns, movingAvgCompute,
            movingAvg5Settings);
    analysisModules.addModule('ema_10', movingAvgColumns, movingAvgCompute,
            movingAvg10Settings);

}());