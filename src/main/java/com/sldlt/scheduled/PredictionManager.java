package com.sldlt.scheduled;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSPredictionDto;
import com.sldlt.navps.dto.PredictionResultsDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSPredictionService;
import com.sldlt.navps.service.NAVPSPredictorService;

@Component
public class PredictionManager {

    private static final Logger LOG = Logger.getLogger(PredictionManager.class);

    @Autowired
    private FundService fundService;

    @Autowired
    private NAVPSPredictionService navpsPredictionService;

    @Autowired
    private List<NAVPSPredictorService> navpsPredictorServices;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(cron = "${prediction.cron:0 0 5 * * MON}", zone = "${prediction.zone:GMT+8}")
    public void run() {
        List<FundDto> allFunds = fundService.listAllFunds();
        List<Integer> offsets = Arrays.asList(1, 2, 3, 4, 5);
        LocalDate currentDate = LocalDate.now();

        navpsPredictorServices.forEach(predictorService -> allFunds.forEach(fund -> {
            try {
                PredictionResultsDto predictions = predictorService.predict(fund.getCode(), offsets);
                IntStream.range(0, offsets.size()).forEach(index -> {
                    NAVPSPredictionDto prediction = new NAVPSPredictionDto();
                    prediction.setFund(fund.getCode());
                    prediction.setType(predictorService.getType());
                    prediction.setDate(currentDate);
                    prediction.setDaysInAdvance(offsets.get(index));
                    prediction.setValue(predictions.getPredictions().get(index));
                    try {
                        prediction.setParameters(objectMapper.writeValueAsString(predictions.getParameters()));
                    } catch (JsonProcessingException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(prediction);
                    }
                    navpsPredictionService.savePrediction(prediction);
                });
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }));

    }
}
