package com.sldlt.navps.service.impl;

import static com.sldlt.navps.entity.QNAVPSEntry.nAVPSEntry;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.entity.NAVPSEntry;
import com.sldlt.navps.repository.NAVPSEntryRepository;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSService;

@Service
@Transactional
public class NAVPSServiceImpl implements NAVPSService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private NAVPSEntryRepository navpsEntryRepository;

    @Autowired
    private FundService fundService;

    @Override
    public void saveNAVPS(final NAVPSEntryDto entry) {
        saveNAVPS(Collections.singletonList(entry));
    }

    @Override
    public void saveNAVPS(final List<NAVPSEntryDto> entries) {
        navpsEntryRepository.save(entries.stream()
            .filter(
                entry -> navpsEntryRepository.count(nAVPSEntry.date.eq(entry.getDate()).and(nAVPSEntry.fund.eq(entry.getFund()))) == 0)
            .map(entry -> mapper.map(entry, NAVPSEntry.class)).collect(Collectors.toList()));
    }

    @Override
    public Page<NAVPSEntryDto> listNAVPS(final String fund, final LocalDate dateFrom, final LocalDate dateTo, final Pageable page) {
        final BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.hasText(fund)) {
            predicate.and(nAVPSEntry.fund.eq(fund));
        }
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.date.goe(dateFrom));
        }
        if (dateTo != null) {
            predicate.and(nAVPSEntry.date.loe(dateTo));
        }

        final List<FundDto> funds = fundService.listAllFunds();

        return navpsEntryRepository.findAll(predicate, page).map(entry -> {
            NAVPSEntryDto mappedNavps = mapper.map(entry, NAVPSEntryDto.class);
            mappedNavps.setFundName(getFundName(funds, mappedNavps.getFund()));
            return mappedNavps;
        });
    }

    @Override
    public List<NAVPSEntryDto> listAllNAVPS(final String fund) {
        final BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSEntry.fund.eq(fund));
        return StreamSupport.stream(navpsEntryRepository.findAll(predicate, nAVPSEntry.date.desc()).spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSEntryDto.class)).collect(Collectors.toList());
    }

    private String getFundName(List<FundDto> funds, String fundCode) {
        return funds.stream().filter(fund -> fund.getCode().equals(fundCode)).findFirst().map(FundDto::getName).orElse("");
    }
}
