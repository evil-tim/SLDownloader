package com.sldlt.downloader.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

public interface NAVPSDownloaderService {

    public List<FundDto> findAvailableFunds();

    public List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund, LocalDate limitFrom, LocalDate limitTo)
            throws IOException;

    public List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund) throws IOException;

}
