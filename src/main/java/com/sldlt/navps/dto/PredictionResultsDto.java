package com.sldlt.navps.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PredictionResultsDto {

    List<BigDecimal> predictions;

    Map<String, String> parameters;

    public List<BigDecimal> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<BigDecimal> predictions) {
        this.predictions = predictions;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
