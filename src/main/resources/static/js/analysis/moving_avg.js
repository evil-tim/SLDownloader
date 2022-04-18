(function() {

    function init(_settings) {
        // nothing to do
    }

    function movingAvgColumns(settings) {
        return [ {
            'type' : 'number',
            'title' : '' + settings.period + 'D MA',
            'color' : settings.color
        } ];
    }

    function movingAvgCompute(index, data, settings) {
        var accum = 0;
        var valid = true;
        for (let i = 0; valid && i < settings.period; i++) {
            var dataIndex = index + i;
            if (dataIndex < data.length) {
                accum += data[dataIndex];
            } else {
                valid = false;
            }
        }
        return [ valid ? accum / settings.period : null ];
    }

    var movingAvg5Settings = {
        'period' : 5,
        'color' : '#FFC300'
    };
    var movingAvg10Settings = {
        'period' : 10,
        'color' : '#FF5733'
    };
    var movingAvg20Settings = {
        'period' : 20,
        'color' : '#C70039'
    };
    var movingAvg40Settings = {
        'period' : 40,
        'color' : '#900C3F'
    };
    var movingAvg80Settings = {
        'period' : 80,
        'color' : '#581845'
    };
    var movingAvg160Settings = {
        'period' : 160,
        'color' : '#370f2b'
    };

    analysisModules.addModule('ma_5', movingAvgColumns, init, movingAvgCompute,
            movingAvg5Settings);
    analysisModules.addModule('ma_10', movingAvgColumns, init,
            movingAvgCompute, movingAvg10Settings);
    analysisModules.addModule('ma_20', movingAvgColumns, init,
            movingAvgCompute, movingAvg20Settings);
    analysisModules.addModule('ma_40', movingAvgColumns, init,
            movingAvgCompute, movingAvg40Settings);
    analysisModules.addModule('ma_80', movingAvgColumns, init,
            movingAvgCompute, movingAvg80Settings);
    analysisModules.addModule('ma_160', movingAvgColumns, init,
            movingAvgCompute, movingAvg160Settings);

}());