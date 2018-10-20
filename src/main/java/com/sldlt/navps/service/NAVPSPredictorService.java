package com.sldlt.navps.service;

import java.util.List;

import com.sldlt.navps.dto.PredictionResultsDto;

public interface NAVPSPredictorService {

    String getType();

    PredictionResultsDto predict(String fund, List<Integer> daysAdvance);

}
