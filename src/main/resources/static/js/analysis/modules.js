var analysisModules = (function() {

    var _analysisModuleNames = [];
    var _analysisModuleMap = {};

    var analysisModules = {};

    analysisModules.addModule = function(name, columnsFunction, initFunction,
            computeFunction, settings) {
        _analysisModuleNames.push(name);
        _analysisModuleMap[name] = {
            'columnsFunction' : columnsFunction,
            'initFunction' : initFunction,
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

    analysisModules.init = function(name) {
        _analysisModuleMap[name]
                .initFunction(_analysisModuleMap[name].settings);
    }

    analysisModules.compute = function(name, index, data) {
        return _analysisModuleMap[name].computeFunction(index, data,
                _analysisModuleMap[name].settings);
    }

    return analysisModules;
}());