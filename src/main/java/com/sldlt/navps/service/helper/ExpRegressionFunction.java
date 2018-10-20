package com.sldlt.navps.service.helper;

public class ExpRegressionFunction implements RegressionFunction {

    private double pow = 1;

    public ExpRegressionFunction(final double pow) {
        this.pow = pow;
    }

    @Override
    public double compute(final double value) {
        return Math.pow(value, pow);
    }

    @Override
    public String getName() {
        return "exponential " + pow + " deg";
    }

}
