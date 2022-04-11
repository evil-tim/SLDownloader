package com.sldlt.navps.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.PredictionResultsDto;

public abstract class AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl
    extends AbstractNonlinearRegressionNAVPSPredictorServiceImpl {

    protected AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl() {
        super();
    }

    @Override
    public String getType() {
        return "Random Split Nonlinear Regression " + getRangeType();
    }

    @Override
    public PredictionResultsDto predict(final String fund, final List<Integer> daysAdvance) {
        final List<Pair<BigDecimal, BigDecimal>> navpsData = fetchNavpsData(fund, getMaxRange());
        final List<List<Pair<BigDecimal, BigDecimal>>> splitNavpsData = splitNavpsData(navpsData, 4, 0.5);

        final List<PredictionResultsDto> splitResults = splitNavpsData.stream().map(this::makeParameters).map(parameters -> {
            final PredictionResultsDto results = new PredictionResultsDto();
            results.setPredictions(daysAdvance.stream().map(days -> makePrediction(parameters, days)).toList());
            results.setParameters(convertParameters(parameters));
            return results;
        }).toList();

        final int numPredictions = daysAdvance.size();
        final PredictionResultsDto finalResults = new PredictionResultsDto();
        finalResults.setPredictions(new ArrayList<>());
        finalResults.getPredictions().addAll(Collections.nCopies(numPredictions, BigDecimal.ZERO));
        finalResults.setParameters(new HashMap<>());

        for (int i = 0; i < splitResults.size(); i++) {
            final int currentIndex = i;
            final List<BigDecimal> splitPredictions = splitResults.get(i).getPredictions();
            for (int j = 0; j < numPredictions; j++) {
                finalResults.getPredictions().set(j, finalResults.getPredictions().get(j).add(splitPredictions.get(j)));
            }
            splitResults.get(i).getParameters().entrySet().stream()
                .forEach(entry -> finalResults.getParameters().put(entry.getKey() + "_" + currentIndex, entry.getValue()));
        }
        for (int i = 0; i < numPredictions; i++) {
            finalResults.getPredictions().set(i, finalResults.getPredictions().get(i).divide(BigDecimal.valueOf(splitResults.size())));
        }

        return finalResults;

    }

    private List<List<Pair<BigDecimal, BigDecimal>>> splitNavpsData(final List<Pair<BigDecimal, BigDecimal>> navpsData, final int numSplit,
        final double includeChance) {
        final List<List<Pair<BigDecimal, BigDecimal>>> splitNavps = new LinkedList<>();
        for (int i = 0; i < numSplit; i++) {
            splitNavps.add(navpsData.stream().filter(value -> Math.random() < includeChance).toList());
        }
        return splitNavps;
    }

    @Override
    protected abstract String getRangeType();

    @Override
    protected abstract LocalDate getMaxRange();

}
