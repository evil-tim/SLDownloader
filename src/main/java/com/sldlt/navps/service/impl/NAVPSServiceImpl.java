package com.sldlt.navps.service.impl;

import static com.sldlt.navps.entity.QNAVPSEntry.nAVPSEntry;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.entity.NAVPSEntry;
import com.sldlt.navps.repository.NAVPSEntryRepository;
import com.sldlt.navps.service.NAVPSService;

@Service
@Transactional
public class NAVPSServiceImpl implements NAVPSService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private NAVPSEntryRepository navpsEntryRepository;

    @Override
    public void saveNAVPS(NAVPSEntryDto entry) {
        saveNAVPS(Collections.singletonList(entry));
    }

    @Override
    public void saveNAVPS(List<NAVPSEntryDto> entries) {
        navpsEntryRepository.save(entries.stream()
                .filter(entry -> navpsEntryRepository
                        .count(nAVPSEntry.date.eq(entry.getDate()).and(nAVPSEntry.fund.eq(entry.getFund()))) == 0)
                .map(entry -> mapper.map(entry, NAVPSEntry.class)).collect(Collectors.toList()));

    }

    @Override
    public Page<NAVPSEntryDto> listNAVPS(String fund, LocalDate dateFrom, LocalDate dateTo, Pageable page) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSEntry.fund.eq(fund));
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.date.goe(dateFrom));
        }
        if (dateTo != null) {
            predicate.and(nAVPSEntry.date.loe(dateTo));
        }
        Page<NAVPSEntry> result = navpsEntryRepository.findAll(predicate, page);
        return result.map(new Converter<NAVPSEntry, NAVPSEntryDto>() {
            @Override
            public NAVPSEntryDto convert(NAVPSEntry source) {
                return mapper.map(source, NAVPSEntryDto.class);
            }
        });
    }

}
