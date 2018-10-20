package com.sldlt.navps.service.helper;

public class CosRegressionFunction implements RegressionFunction {

    private double period = 1.0;
    private double factor = Math.PI * 2.0;

    public CosRegressionFunction(final double period) {
        this.period = period;
        this.factor /= this.period;
    }

    @Override
    public double compute(final double value) {
        return Math.cos(value * factor);
    }

    @Override
    public String getName() {
        return "cosine " + period + " period";
    }

}
