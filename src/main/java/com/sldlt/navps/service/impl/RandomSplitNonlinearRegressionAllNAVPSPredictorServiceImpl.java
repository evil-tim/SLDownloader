package com.sldlt.navps.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class RandomSplitNonlinearRegressionAllNAVPSPredictorServiceImpl
    extends AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl {

    @Override
    protected String getRangeType() {
        return "All";
    }

    @Override
    protected LocalDate getMaxRange() {
        return null;
    }

}
