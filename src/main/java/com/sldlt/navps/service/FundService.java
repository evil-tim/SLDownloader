package com.sldlt.navps.service;

import java.util.List;

import com.sldlt.navps.dto.FundDto;

public interface FundService {

    FundDto saveFund(FundDto fund);

    List<FundDto> listAllFunds();

}
