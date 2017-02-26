package com.sldlt.navps.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSService;

@Service
public class NAVPSServiceImpl implements NAVPSService {

    @Override
    public void saveNAVPS(NAVPSEntryDto entry) {
        saveNAVPS(Collections.singletonList(entry));
    }

    @Override
    public void saveNAVPS(List<NAVPSEntryDto> entry) {
        // TODO Auto-generated method stub

    }

    @Override
    public Page<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, Pageable page) {
        // TODO Auto-generated method stub
        return null;
    }

}
