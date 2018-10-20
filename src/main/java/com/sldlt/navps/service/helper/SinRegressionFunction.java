package com.sldlt.navps.service.helper;

public class SinRegressionFunction implements RegressionFunction {

    private double period = 1;
    private double factor = Math.PI * 2.0;

    public SinRegressionFunction(final double period) {
        this.period = period;
        this.factor /= this.period;
    }

    @Override
    public double compute(final double value) {
        return Math.sin(value * factor);
    }

    @Override
    public String getName() {
        return "sine " + period + " period";
    }

}
