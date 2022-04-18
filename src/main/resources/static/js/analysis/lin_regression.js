(function() {

    function init(settings) {
        settings.compute = false;
        settings.alpha = null;
        settings.beta = null;
        settings.variance = null;
    }

    function columns(settings) {
        return [ {
            'type' : 'number',
            'title' : 'Linear Regresion',
            'color' : settings.color1
        } , {
            'type' : 'number',
            'title' : 'Linear Regresion + VAR',
            'color' : settings.color2
        } , {
            'type' : 'number',
            'title' : 'Linear Regresion - VAR',
            'color' : settings.color2
        } ];
    }

    function compute(index, data, settings) {
        if (!settings.compute) {
            var linRegData = linearReg(data);
            settings.compute = true;
            settings.alpha = linRegData.alpha;
            settings.beta = linRegData.beta;
            settings.variance = linRegData.variance;
        }

        var linRegVal = settings.alpha + settings.beta * index;
        return [linRegVal, linRegVal - settings.variance, linRegVal + settings.variance];
    }

    function linearReg(dataY) {
        var meanX = 0;
        var meanY = 0;
        var varX = 0;
        var varY = 0;
        var covXY = 0;
        var n = 0;

        for (let i = 0; i < dataY.length; i++) {
            n++;

            var dx = i - meanX;
            var dy = dataY[i] - meanY;

            varX += (((n - 1) / n) * dx * dx - varX) / n;
            varY += (((n - 1) / n) * dy * dy - varY) / n;
            covXY += (((n - 1) / n) * dx * dy - covXY) / n;

            meanX += dx / n;
            meanY += dy / n;
        }

        return {
            'alpha' : meanY - covXY * meanX / varX,
            'beta' :  covXY / varX,
            'variance' : Math.sqrt(varY)
        };
    }

    var linearRegressionSettings = {
        'color1' : '#2471a3',
        'color2' : '#7fb3d5',
        'compute' : false,
        'alpha' : null,
        'beta' : null,
        'variance' : null
    };

    analysisModules.addModule('lin_reg', columns, init, compute,
            linearRegressionSettings);

}());