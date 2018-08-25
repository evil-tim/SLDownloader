(function() {

    function init(settings) {

    }

    function donchianColumns(settings) {
        return [ {
            'type' : 'number',
            'title' : '' + settings.period + 'D UPPER',
            'color' : settings.color1
        }, {
            'type' : 'number',
            'title' : '' + settings.period + 'D LOWER',
            'color' : settings.color2
        } ];
    }

    function donchianCompute(index, data, settings) {
        var min = null;
        var max = null;
        var valid = true;
        for (var i = 0; valid && i < settings.period; i++) {
            var dataIndex = index + i;
            if (dataIndex < data.length) {
                if (min === null || min > data[dataIndex]) {
                    min = data[dataIndex];
                }
                if (max === null || max < data[dataIndex]) {
                    max = data[dataIndex];
                }
            } else {
                valid = false;
            }
        }
        return [ max, min ];
    }

    var donchian20Settings = {
        'period' : 20,
        'color1' : '#BF360C',
        'color2' : '#BF360C'
    };
    var donchian80Settings = {
        'period' : 80,
        'color1' : '#DF714F',
        'color2' : '#DF714F'
    };
    var donchian160Settings = {
        'period' : 160,
        'color1' : '#FFAB91',
        'color2' : '#FFAB91'
    };

    analysisModules.addModule('dc_20', donchianColumns, init, donchianCompute,
            donchian20Settings);
    analysisModules.addModule('dc_80', donchianColumns, init, donchianCompute,
            donchian80Settings);
    analysisModules.addModule('dc_160', donchianColumns, init, donchianCompute,
            donchian160Settings);

}());