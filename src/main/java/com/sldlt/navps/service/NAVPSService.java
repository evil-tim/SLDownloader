package com.sldlt.navps.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSService {

    void saveNAVPS(NAVPSEntryDto entry);

    void saveNAVPS(List<NAVPSEntryDto> entry);

    List<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo);

    Page<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, Pageable page);

    List<NAVPSEntryDto> listAllNAVPS(String fund);

}
