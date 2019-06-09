package com.sldlt.navps.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.PredictionResultsDto;
import com.sldlt.navps.service.helper.ConstantRegressionFunction;
import com.sldlt.navps.service.helper.CosRegressionFunction;
import com.sldlt.navps.service.helper.ExpRegressionFunction;
import com.sldlt.navps.service.helper.LinearRegressionFunction;
import com.sldlt.navps.service.helper.RegressionFunction;
import com.sldlt.navps.service.helper.SinRegressionFunction;

public abstract class AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl extends AbstractNAVPSPredictorServiceImpl {

    private final List<RegressionFunction> functions;

    public AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl() {
        functions = new LinkedList<>();
        functions.add(new ConstantRegressionFunction());
        functions.add(new LinearRegressionFunction());
        functions.add(new ExpRegressionFunction(2.0));
        functions.add(new ExpRegressionFunction(3.0));
        functions.add(new SinRegressionFunction(2.5));
        functions.add(new SinRegressionFunction(3.0));
        functions.add(new SinRegressionFunction(4.0));
        functions.add(new SinRegressionFunction(5.0));
        functions.add(new SinRegressionFunction(6.0));
        functions.add(new SinRegressionFunction(7.0));
        functions.add(new SinRegressionFunction(8.0));
        functions.add(new SinRegressionFunction(9.0));
        functions.add(new SinRegressionFunction(10.0));
        functions.add(new SinRegressionFunction(11.0));
        functions.add(new SinRegressionFunction(12.0));
        functions.add(new SinRegressionFunction(13.0));
        functions.add(new SinRegressionFunction(14.0));
        functions.add(new SinRegressionFunction(15.0));
        functions.add(new SinRegressionFunction(16.0));
        functions.add(new SinRegressionFunction(17.0));
        functions.add(new SinRegressionFunction(18.0));
        functions.add(new SinRegressionFunction(19.0));
        functions.add(new SinRegressionFunction(20.0));
        functions.add(new SinRegressionFunction(21.0));
        functions.add(new SinRegressionFunction(22.0));
        functions.add(new SinRegressionFunction(23.0));
        functions.add(new SinRegressionFunction(24.0));
        functions.add(new SinRegressionFunction(25.0));
        functions.add(new SinRegressionFunction(26.0));
        functions.add(new SinRegressionFunction(27.0));
        functions.add(new SinRegressionFunction(28.0));
        functions.add(new SinRegressionFunction(29.0));
        functions.add(new SinRegressionFunction(30.0));
        functions.add(new SinRegressionFunction(31.0));
        functions.add(new SinRegressionFunction(32.0));
        functions.add(new SinRegressionFunction(33.0));
        functions.add(new SinRegressionFunction(34.0));
        functions.add(new SinRegressionFunction(35.0));
        functions.add(new SinRegressionFunction(36.0));
        functions.add(new SinRegressionFunction(37.0));
        functions.add(new SinRegressionFunction(38.0));
        functions.add(new SinRegressionFunction(39.0));
        functions.add(new SinRegressionFunction(40.0));
        functions.add(new SinRegressionFunction(60.0));
        functions.add(new SinRegressionFunction(120.0));
        functions.add(new SinRegressionFunction(240.0));
        functions.add(new SinRegressionFunction(480.0));
        functions.add(new CosRegressionFunction(2.5));
        functions.add(new CosRegressionFunction(3.0));
        functions.add(new CosRegressionFunction(4.0));
        functions.add(new CosRegressionFunction(5.0));
        functions.add(new CosRegressionFunction(6.0));
        functions.add(new CosRegressionFunction(7.0));
        functions.add(new CosRegressionFunction(8.0));
        functions.add(new CosRegressionFunction(9.0));
        functions.add(new CosRegressionFunction(10.0));
        functions.add(new CosRegressionFunction(11.0));
        functions.add(new CosRegressionFunction(12.0));
        functions.add(new CosRegressionFunction(13.0));
        functions.add(new CosRegressionFunction(14.0));
        functions.add(new CosRegressionFunction(15.0));
        functions.add(new CosRegressionFunction(16.0));
        functions.add(new CosRegressionFunction(17.0));
        functions.add(new CosRegressionFunction(18.0));
        functions.add(new CosRegressionFunction(19.0));
        functions.add(new CosRegressionFunction(20.0));
        functions.add(new CosRegressionFunction(21.0));
        functions.add(new CosRegressionFunction(22.0));
        functions.add(new CosRegressionFunction(23.0));
        functions.add(new CosRegressionFunction(24.0));
        functions.add(new CosRegressionFunction(25.0));
        functions.add(new CosRegressionFunction(26.0));
        functions.add(new CosRegressionFunction(27.0));
        functions.add(new CosRegressionFunction(28.0));
        functions.add(new CosRegressionFunction(29.0));
        functions.add(new CosRegressionFunction(30.0));
        functions.add(new CosRegressionFunction(31.0));
        functions.add(new CosRegressionFunction(32.0));
        functions.add(new CosRegressionFunction(33.0));
        functions.add(new CosRegressionFunction(34.0));
        functions.add(new CosRegressionFunction(35.0));
        functions.add(new CosRegressionFunction(36.0));
        functions.add(new CosRegressionFunction(37.0));
        functions.add(new CosRegressionFunction(38.0));
        functions.add(new CosRegressionFunction(39.0));
        functions.add(new CosRegressionFunction(40.0));
        functions.add(new CosRegressionFunction(60.0));
        functions.add(new CosRegressionFunction(120.0));
        functions.add(new CosRegressionFunction(240.0));
        functions.add(new CosRegressionFunction(480.0));
    }

    @Override
    public String getType() {
        return "Random Split Nonlinear Regression " + getRangeType();
    }

    @Override
    public PredictionResultsDto predict(final String fund, final List<Integer> daysAdvance) {
        final List<Pair<BigDecimal, BigDecimal>> navpsData = fetchNavpsData(fund, getMaxRange());
        final List<List<Pair<BigDecimal, BigDecimal>>> splitNavpsData = splitNavpsData(navpsData, 4, 0.5);

        List<PredictionResultsDto> splitResults = splitNavpsData.stream().map(this::makeParameters).map(parameters -> {
            final PredictionResultsDto results = new PredictionResultsDto();
            results.setPredictions(daysAdvance.stream().map(days -> makePrediction(parameters, days)).collect(Collectors.toList()));
            results.setParameters(convertParameters(parameters));
            return results;
        }).collect(Collectors.toList());

        PredictionResultsDto finalResults = new PredictionResultsDto();
        finalResults.setPredictions(daysAdvance.stream().map(day -> BigDecimal.ZERO).collect(Collectors.toList()));
        finalResults.setParameters(new HashMap<>());
        for (int i = 0; i < splitResults.size(); i++) {
            final int currentIndex = i;
            List<BigDecimal> splitPredictions = splitResults.get(i).getPredictions();
            for (int j = 0; j < daysAdvance.size(); j++) {
                finalResults.getPredictions().set(j, finalResults.getPredictions().get(j).add(splitPredictions.get(j)));
            }
            splitResults.get(i).getParameters().entrySet().stream().forEach(entry -> {
                finalResults.getParameters().put(entry.getKey() + "_" + currentIndex, entry.getValue());
            });
        }
        for (int i = 0; i < daysAdvance.size(); i++) {
            finalResults.getPredictions().set(i, finalResults.getPredictions().get(i).divide(BigDecimal.valueOf(splitResults.size())));
        }

        return finalResults;

    }

    private List<List<Pair<BigDecimal, BigDecimal>>> splitNavpsData(final List<Pair<BigDecimal, BigDecimal>> navpsData,
        final int numSplit, final double includeChance) {
        List<List<Pair<BigDecimal, BigDecimal>>> splitNavps = new LinkedList<>();
        for (int i = 0; i < numSplit; i++) {
            splitNavps.add(navpsData.stream().filter(value -> {
                return Math.random() < includeChance;
            }).collect(Collectors.toList()));
        }
        return splitNavps;
    }

    protected abstract String getRangeType();

    protected abstract LocalDate getMaxRange();

    private RealMatrix makeParameters(List<Pair<BigDecimal, BigDecimal>> navpsData) {
        final int size = navpsData.size();

        final double[][] xMatrixData = new double[size][functions.size()];
        final double[][] yMatrixData = new double[size][1];

        for (int i = 0; i < size; i++) {
            final double xvalue = navpsData.get(i).getFirst().doubleValue();
            List<Double> xmatrixRow = functions.stream().map(function -> function.compute(xvalue)).collect(Collectors.toList());
            for (int j = 0; j < functions.size(); j++) {
                xMatrixData[i][j] = xmatrixRow.get(j);
            }
            final double yvalue = navpsData.get(i).getSecond().doubleValue();
            yMatrixData[i][0] = yvalue;

        }

        final RealMatrix xmatrix = MatrixUtils.createRealMatrix(xMatrixData);
        final RealMatrix ymatrix = MatrixUtils.createRealMatrix(yMatrixData);
        final RealMatrix xmatrixTrans = xmatrix.transpose();
        final RealMatrix mul = xmatrixTrans.multiply(xmatrix);
        final RealMatrix inv = MatrixUtils.inverse(mul);
        final RealMatrix mul2 = inv.multiply(xmatrixTrans);
        final RealMatrix res = mul2.multiply(ymatrix);
        return res;
    }

    private BigDecimal makePrediction(final RealMatrix parameters, final int daysAdvance) {
        return BigDecimal.valueOf(IntStream.range(0, functions.size())
            .mapToDouble(i -> parameters.getRow(i)[0] * functions.get(i).compute(daysAdvance)).sum());
    }

    private Map<String, String> convertParameters(final RealMatrix parameters) {
        final Map<String, String> exportedParams = new LinkedHashMap<>();

        IntStream.range(0, functions.size()).forEach(index -> {
            exportedParams.put(functions.get(index).getName(), Double.toString(parameters.getRow(index)[0]));
        });

        return exportedParams;
    }
}
