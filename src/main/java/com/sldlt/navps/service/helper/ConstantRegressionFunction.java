package com.sldlt.navps.service.helper;

public class ConstantRegressionFunction implements RegressionFunction {

    @Override
    public double compute(final double value) {
        return 1;
    }

    @Override
    public String getName() {
        return "constant";
    }

}
