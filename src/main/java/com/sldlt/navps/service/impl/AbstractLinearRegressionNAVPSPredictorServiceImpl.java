package com.sldlt.navps.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.PredictionResultsDto;

public abstract class AbstractLinearRegressionNAVPSPredictorServiceImpl extends AbstractNAVPSPredictorServiceImpl {

    @Override
    public String getType() {
        return "Linear Regression " + getRangeType();
    }

    @Override
    public PredictionResultsDto predict(final String fund, final List<Integer> daysAdvance) {
        final List<Pair<BigDecimal, BigDecimal>> navpsData = fetchNavpsData(fund, getMaxRange());
        final Pair<BigDecimal, BigDecimal> parameters = calculateLinearRegressionParameters(navpsData);

        final PredictionResultsDto results = new PredictionResultsDto();
        results.setPredictions(daysAdvance.stream().map(days -> makePrediction(parameters, days)).toList());
        results.setParameters(convertParameters(parameters));

        return results;
    }

    protected abstract String getRangeType();

    protected abstract LocalDate getMaxRange();

    private Pair<BigDecimal, BigDecimal> calculateLinearRegressionParameters(final List<Pair<BigDecimal, BigDecimal>> navpsData) {
        final int size = navpsData.size();

        final Pair<BigDecimal, BigDecimal> totals = navpsData.stream().reduce(Pair.of(BigDecimal.ZERO, BigDecimal.ZERO),
            (data1, data2) -> Pair.of(data1.getFirst().add(data2.getFirst()), data1.getSecond().add(data2.getSecond())));

        final BigDecimal avgX = totals.getFirst().divide(BigDecimal.valueOf(size), 10, RoundingMode.HALF_UP);
        final BigDecimal avgY = totals.getSecond().divide(BigDecimal.valueOf(size), 10, RoundingMode.HALF_UP);

        final BigDecimal covariance = navpsData.stream()
            .map(data -> data.getFirst().subtract(avgX).multiply(data.getSecond().subtract(avgY)))
            .reduce(BigDecimal.ZERO, (value1, value2) -> value1.add(value2));
        final BigDecimal variance = navpsData.stream().map(data -> data.getFirst().subtract(avgX)).map(value -> value.pow(2))
            .reduce(BigDecimal.ZERO, (value1, value2) -> value1.add(value2));

        final BigDecimal beta = covariance.divide(variance, 10, RoundingMode.HALF_UP);

        return Pair.of(beta, avgY.subtract(beta.multiply(avgX)));
    }

    private BigDecimal makePrediction(final Pair<BigDecimal, BigDecimal> parameters, final int daysAdvance) {
        return parameters.getFirst().multiply(BigDecimal.valueOf(daysAdvance)).add(parameters.getSecond());
    }

    private Map<String, String> convertParameters(final Pair<BigDecimal, BigDecimal> parameters) {
        final Map<String, String> exportedParams = new LinkedHashMap<>();
        exportedParams.put("alpha", parameters.getFirst().toString());
        exportedParams.put("beta", parameters.getSecond().toString());
        return exportedParams;
    }
}
