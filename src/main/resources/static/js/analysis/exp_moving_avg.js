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
        'color' : '#E5FCC2',
        'emaCache' : []
    };
    var movingAvg10Settings = {
        'period' : 10,
        'color' : '#9DE0AD',
        'emaCache' : []
    };
    var movingAvg20Settings = {
        'period' : 20,
        'color' : '#71C6AA',
        'emaCache' : []
    };
    var movingAvg40Settings = {
        'period' : 40,
        'color' : '#45ADA8',
        'emaCache' : []
    };
    var movingAvg80Settings = {
        'period' : 80,
        'color' : '#4C9394',
        'emaCache' : []
    };
    var movingAvg160Settings = {
        'period' : 160,
        'color' : '#547980',
        'emaCache' : []
    };

    analysisModules.addModule('ema_5', movingAvgColumns, init,
            movingAvgCompute, movingAvg5Settings);
    analysisModules.addModule('ema_10', movingAvgColumns, init,
            movingAvgCompute, movingAvg10Settings);
    analysisModules.addModule('ema_20', movingAvgColumns, init,
            movingAvgCompute, movingAvg20Settings);
    analysisModules.addModule('ema_40', movingAvgColumns, init,
            movingAvgCompute, movingAvg40Settings);
    analysisModules.addModule('ema_80', movingAvgColumns, init,
            movingAvgCompute, movingAvg80Settings);
    analysisModules.addModule('ema_160', movingAvgColumns, init,
            movingAvgCompute, movingAvg160Settings);

}());