package com.sldlt.downloader.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSDownloaderService {

    List<FundDto> findAvailableFunds();

    List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund, LocalDate limitFrom, LocalDate limitTo) throws IOException;

    List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund) throws IOException;

}
