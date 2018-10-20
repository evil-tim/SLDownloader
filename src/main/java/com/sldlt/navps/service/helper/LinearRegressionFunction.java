package com.sldlt.navps.service.helper;

public class LinearRegressionFunction implements RegressionFunction {

    @Override
    public double compute(final double value) {
        return value;
    }

    @Override
    public String getName() {
        return "linear";
    }

}
