package com.sldlt.downloader.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.sldlt.downloader.entity.request.NAVPSRequest;
import com.sldlt.downloader.entity.response.FundsResponse;
import com.sldlt.downloader.entity.response.NAVPSResponse;
import com.sldlt.downloader.exception.NAVPSDownloadValidationException;
import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

@Service
public class NAVPSDownloaderServiceImpl implements NAVPSDownloaderService {

    private static final Logger LOG = LogManager.getLogger(NAVPSDownloaderServiceImpl.class);

    private final DateTimeFormatter responseDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DateTimeFormatter paramDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${navps.fundslist.url}")
    private String fundsUrl;

    @Value("${navps.navpsvalue.url}")
    private String navpsUrl;

    @Value("${task.updater.zone:GMT+8}")
    private String timeZone;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpHeaders httpHeaders;

    @Override
    public List<FundDto> findAvailableFunds() {
        final HttpEntity<NAVPSRequest> requestEntity = new HttpEntity<>(httpHeaders);

        final FundsResponse response = restTemplate.postForObject(fundsUrl, requestEntity, FundsResponse.class);

        if (response == null) {
            return Collections.emptyList();
        }

        final List<FundDto> result = new ArrayList<>();
        if (response.getPesoList() != null && !response.getPesoList().isEmpty()) {
            result.addAll(response.getPesoList().stream().filter(fundResponse -> StringUtils.hasText(fundResponse.getFundCode()))
                .map(fundResponse -> {
                    final FundDto fund = new FundDto();
                    fund.setCode(fundResponse.getFundCode().trim());
                    fund.setName(cleanFundName(fundResponse.getFundName()));
                    return fund;
                }).toList());
        }

        if (response.getDollarList() != null && !response.getDollarList().isEmpty()) {
            result.addAll(response.getDollarList().stream().filter(fundResponse -> StringUtils.hasText(fundResponse.getFundCode()))
                .map(fundResponse -> {
                    final FundDto fund = new FundDto();
                    fund.setCode(fundResponse.getFundCode().trim());
                    fund.setName(cleanFundName(fundResponse.getFundName()));
                    return fund;
                }).toList());
        }

        return result;
    }

    private String cleanFundName(String fundName) {
        if (!StringUtils.hasText(fundName)) {
            return "";
        }
        return "Sun Life " + Arrays.stream(fundName.split(" ")).filter(StringUtils::hasText).map(String::toLowerCase)
            .map(this::capitalizeWord).collect(Collectors.joining(" ")).replace("Sun Life ", "");
    }

    private String capitalizeWord(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(final FundDto fund) throws IOException {
        final LocalDate currentDate = LocalDate.now(ZoneId.of(timeZone));
        return fetchNAVPSFromPage(fund, currentDate, currentDate);
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(final FundDto fund, final LocalDate limitFrom,
        final LocalDate limitTo) throws IOException {
        final NAVPSRequest request = new NAVPSRequest();
        request.setFundCode(fund.getCode());
        request.setDateFrom(limitFrom.format(paramDateFormat));
        request.setDateTo(limitTo.format(paramDateFormat));

        final HttpEntity<NAVPSRequest> requestEntity = new HttpEntity<>(request, httpHeaders);

        final NAVPSResponse[] response = restTemplate.postForObject(navpsUrl, requestEntity, NAVPSResponse[].class);

        final List<NAVPSEntryDto> result = Arrays.stream(response).map(navpsResponse -> {
            NAVPSEntryDto entry = new NAVPSEntryDto();
            entry.setFund(navpsResponse.getFundCode());
            entry.setEntryDate(LocalDate.parse(navpsResponse.getFundValDate(), responseDateFormat));
            entry.setFundValue(new BigDecimal(navpsResponse.getFundNetVal()));
            return entry;
        }).toList();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Downloaded NAVPS [{}]", result);
        }

        validateResultsNotEmpty(result);

        validateResultsInRange(result, limitFrom, limitTo);

        return result;
    }

    private void validateResultsNotEmpty(final List<NAVPSEntryDto> result) {
        if (result == null || result.isEmpty()) {
            throw new NAVPSDownloadValidationException("No entries found");
        }
    }

    private void validateResultsInRange(final List<NAVPSEntryDto> result, final LocalDate limitFrom, final LocalDate limitTo) {
        result.stream().filter(entry -> entry.getEntryDate().isBefore(limitFrom) || entry.getEntryDate().isAfter(limitTo)).findAny()
            .ifPresent(entry -> {
                throw new NAVPSDownloadValidationException("Found out of range entry - " + entry);
            });
    }

}
