(function() {

    function init(_settings) {
        // nothing to do
    }

    function columns(settings) {
        return [ {
            'type' : 'number',
            'title' : '' + settings.period + 'D BB',
            'color' : settings.color1
        }, {
            'type' : 'number',
            'title' : '' + settings.period + 'D BB Upper',
            'color' : settings.color2
        }, {
            'type' : 'number',
            'title' : '' + settings.period + 'D BB Lower',
            'color' : settings.color2
        } ];
    }

    function compute(index, data, settings) {
        var accum = 0;
        var stdDevAccum = 0;
        var valid = true;
        for (let i = 0; valid && i < settings.period; i++) {
            let dataIndex = index + i;
            if (dataIndex < data.length) {
                accum += data[dataIndex];
            } else {
                valid = false;
            }
        }
        if (valid) {
            var average = accum / settings.period;

            for (let i = 0; i < settings.period; i++) {
                let dataIndex = index + i;
                stdDevAccum += (data[dataIndex] - average)
                        * (data[dataIndex] - average);
            }

            var stdDev = Math.sqrt(stdDevAccum / (settings.period - 1));
            return [ average, average + stdDev * 2, average - stdDev * 2 ];
        } else {
            return [ null, null, null ];
        }
    }

    var bb20Settings = {
        'period' : 20,
        'color1' : '#d81b60',
        'color2' : '#f06292'
    };
    var bb80Settings = {
        'period' : 80,
        'color1' : '#b32085',
        'color2' : '#d565ad'
    };
    var bb160Settings = {
        'period' : 160,
        'color1' : '#8e24aa',
        'color2' : '#ba68c8'
    };

    analysisModules.addModule('bb_20', columns, init, compute, bb20Settings);
    analysisModules.addModule('bb_80', columns, init, compute, bb80Settings);
    analysisModules.addModule('bb_160', columns, init, compute, bb160Settings);

}());