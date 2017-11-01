package com.sldlt.navps.service.impl;

import static com.sldlt.navps.entity.QFund.fund;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.entity.Fund;
import com.sldlt.navps.repository.FundRepository;
import com.sldlt.navps.service.FundService;

@Service
@Transactional
public class FundServiceImpl implements FundService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private FundRepository fundRepository;

    @Override
    public FundDto saveFund(final FundDto newFund) {
        Fund fundObj = fundRepository.findOne(new BooleanBuilder().and(fund.code.eq(newFund.getCode())));
        if (fundObj == null) {
            fundObj = mapper.map(newFund, Fund.class);
        } else {
            mapper.map(newFund, fundObj);
        }
        return mapper.map(fundRepository.save(fundObj), FundDto.class);
    }

    @Override
    public List<FundDto> listAllFunds() {
        return fundRepository.findAll().stream().map(fund -> mapper.map(fund, FundDto.class)).collect(Collectors.toList());
    }

    @Override
    public FundDto getFundByCode(final String code) {
        return mapper.map(fundRepository.findOneByCode(code), FundDto.class);
    }

}
