package com.sldlt.navps.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSPredictorService;
import com.sldlt.navps.service.NAVPSService;

public abstract class AbstractNAVPSPredictorServiceImpl implements NAVPSPredictorService {

    @Autowired
    private NAVPSService navpsService;

    protected List<Pair<BigDecimal, BigDecimal>> fetchNavpsData(final String fund, final LocalDate maxRange) {
        final List<NAVPSEntryDto> navpsEntries = navpsService.listNAVPS(fund, maxRange, null);
        return IntStream.range(0, navpsEntries.size()).mapToObj(i -> Pair.of(BigDecimal.valueOf(-i), navpsEntries.get(i).getValue()))
            .toList();
    }

}
