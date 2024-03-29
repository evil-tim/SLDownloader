package com.sldlt.navps.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSService {

    void saveNAVPS(NAVPSEntryDto entry);

    void saveNAVPS(List<NAVPSEntryDto> entry);

    List<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo);

    Page<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, Pageable page);

    Map<String, List<NAVPSEntryDto>> listNAVPS(Set<String> funds, LocalDate dateFrom, LocalDate dateTo);

    List<NAVPSEntryDto> listAllNAVPS(String fund);

    Map<String, Map<String, BigDecimal>> listAllCorrelations(LocalDate dateFrom);

    List<Pair<BigDecimal, BigDecimal>> listNAVPSPaired(String fundX, String fundY, LocalDate dateFrom, LocalDate dateTo);

}
