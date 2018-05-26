var analysisModules = (function() {

    var _analysisModuleNames = [];
    var _analysisModuleMap = {};

    var analysisModules = {};

    analysisModules.addModule = function(name, columnsFunction,
            computeFunction, settings) {
        _analysisModuleNames.push(name);
        _analysisModuleMap[name] = {
            'columnsFunction' : columnsFunction,
            'computeFunction' : computeFunction,
            'settings' : settings
        };
    };

    analysisModules.getModules = function() {
        return _analysisModuleNames;
    }

    analysisModules.getColumns = function(name) {
        return _analysisModuleMap[name]
                .columnsFunction(_analysisModuleMap[name].settings);
    }

    analysisModules.compute = function(name, index, data) {
        return _analysisModuleMap[name].computeFunction(index, data,
                _analysisModuleMap[name].settings);
    }

    return analysisModules;
}());