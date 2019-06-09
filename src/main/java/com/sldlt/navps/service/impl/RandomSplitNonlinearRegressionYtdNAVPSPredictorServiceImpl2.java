package com.sldlt.navps.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class RandomSplitNonlinearRegressionYtdNAVPSPredictorServiceImpl2
                extends AbstractRandomSplitNonlinearRegressionNAVPSPredictorServiceImpl {

    @Override
    protected String getRangeType() {
        return "Year to Date";
    }

    @Override
    protected LocalDate getMaxRange() {
        return LocalDate.now().minusYears(1);
    }

}
