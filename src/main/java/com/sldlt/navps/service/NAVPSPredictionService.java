package com.sldlt.navps.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.sldlt.navps.dto.NAVPSPredictionDto;

public interface NAVPSPredictionService {

    NAVPSPredictionDto savePrediction(NAVPSPredictionDto prediction);

    Map<LocalDate, Set<NAVPSPredictionDto>> getPredictions(String fund, String predictionType, LocalDate dateFrom, LocalDate dateTo);

    Set<String> getPredictionsTypes();

}
