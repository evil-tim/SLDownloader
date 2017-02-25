package com.sldlt.downloader.service;

import java.time.LocalDate;
import java.util.List;

import com.sldlt.entity.NAVPSEntry;

public interface NAVPSDownloader {

    public List<String> findAvailableFunds();

    public List<NAVPSEntry> fetchNAVPSFromPage(String fund, LocalDate limitFrom, LocalDate limitTo);

    public List<NAVPSEntry> fetchNAVPSFromPage(String fund);

}
