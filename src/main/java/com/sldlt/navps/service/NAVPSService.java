package com.sldlt.navps.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSService {

    void saveNAVPS(NAVPSEntryDto entry);

    void saveNAVPS(List<NAVPSEntryDto> entry);

    List<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo);

    List<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, boolean withFundDetail);

    Page<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, Pageable page);

    List<NAVPSEntryDto> listAllNAVPS(String fund);

    Map<String, Map<String, BigDecimal>> listAllCorrelations(LocalDate dateFrom);

    List<Pair<BigDecimal, BigDecimal>> listNAVPSPaired(String fundX, String fundY, LocalDate dateFrom, LocalDate dateTo);

}
