package com.sldlt.downloader.service;

import java.time.LocalDate;
import java.util.List;

import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSDownloaderService {

    public List<FundDto> findAvailableFunds();

    public List<NAVPSEntryDto> fetchNAVPSFromPage(String fund, LocalDate limitFrom, LocalDate limitTo);

    public List<NAVPSEntryDto> fetchNAVPSFromPage(String fund);

}
