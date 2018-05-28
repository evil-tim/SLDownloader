(function() {

    function init(settings) {
        settings.emaCache = [];
    }

    function movingAvgColumns(settings) {
        return [ {
            'type' : 'number',
            'title' : '' + settings.period + 'D EMA',
            'color' : settings.color
        } ];
    }

    function movingAvgCompute(index, data, settings) {

        if (settings.emaCache[index]) {
            return [ settings.emaCache[index] ];
        }

        var ema = null;
        if (index == data.length - 1) {
            ema = data[index];
        } else if (index < data.length - 1) {

            if (!settings.factor) {
                settings.factor = 2 / (settings.period + 1);
            }

            ema = (settings.factor * data[index])
                    + ((1 - settings.factor) * movingAvgCompute(index + 1,
                            data, settings));
        }

        settings.emaCache[index] = ema;
        return [ ema ];
    }

    var movingAvg5Settings = {
        'period' : 5,
        'color' : '#DDC333',
        'emaCache' : []
    };
    var movingAvg10Settings = {
        'period' : 10,
        'color' : '#DD5766',
        'emaCache' : []
    };

    analysisModules.addModule('ema_5', movingAvgColumns, init,
            movingAvgCompute, movingAvg5Settings);
    analysisModules.addModule('ema_10', movingAvgColumns, init,
            movingAvgCompute, movingAvg10Settings);

}());