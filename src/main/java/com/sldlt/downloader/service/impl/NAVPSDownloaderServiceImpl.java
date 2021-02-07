package com.sldlt.downloader.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.sldlt.downloader.entity.request.NAVPSRequest;
import com.sldlt.downloader.entity.response.NAVPSResponse;
import com.sldlt.downloader.exception.NAVPSDownloadValidationException;
import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

@Service
public class NAVPSDownloaderServiceImpl implements NAVPSDownloaderService {

    private static final Logger LOG = Logger.getLogger(NAVPSDownloaderServiceImpl.class);

    private static final String FUND_NAME_FIELD_NAME = "fundCD";

    private final DateTimeFormatter responseDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DateTimeFormatter paramDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${navps.fundslist.url}")
    private String fundsUrl;

    @Value("${navps.navpsvalue.url}")
    private String navpsUrl;

    @Value("${task.updater.zone:GMT+8}")
    private String timeZone;

    @Override
    public List<FundDto> findAvailableFunds() {
        List<FundDto> result = Collections.emptyList();
        try {
            result = Jsoup.connect(fundsUrl).timeout(60000).get().getElementsByTag("select").stream()
                .filter(element -> element.attr("name").equals(FUND_NAME_FIELD_NAME)).findFirst()
                .map(element -> element.getElementsByTag("option")).orElse(new Elements()).stream()
                .filter(element -> StringUtils.hasText(element.attr("value"))).map(element -> {
                    final FundDto fund = new FundDto();
                    fund.setCode(element.attr("value"));
                    fund.setName("Sun Life " + element.text().replace("Sun Life ", "").trim());
                    return fund;
                }).collect(Collectors.toList());
            if (LOG.isDebugEnabled()) {
                LOG.debug(result);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(final FundDto fund) throws IOException {
        final LocalDate currentDate = LocalDate.now(ZoneId.of(timeZone));
        return fetchNAVPSFromPage(fund, currentDate, currentDate);
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(final FundDto fund, final LocalDate limitFrom, final LocalDate limitTo)
                    throws IOException {

        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(60000);
        clientHttpRequestFactory.setReadTimeout(60000);

        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final NAVPSRequest request = new NAVPSRequest();
        request.setFundCode(fund.getCode());
        request.setDateFrom(limitFrom.format(paramDateFormat));
        request.setDateTo(limitTo.format(paramDateFormat));

        final HttpEntity<NAVPSRequest> requestEntity = new HttpEntity<>(request, headers);

        final NAVPSResponse[] response = restTemplate.postForObject(navpsUrl, requestEntity, NAVPSResponse[].class);

        final List<NAVPSEntryDto> result = Arrays.stream(response).map(navpsResponse -> {
            NAVPSEntryDto entry = new NAVPSEntryDto();
            entry.setFund(navpsResponse.getFundCode());
            entry.setDate(LocalDate.parse(navpsResponse.getFundValDate(), responseDateFormat));
            entry.setValue(new BigDecimal(navpsResponse.getFundNetVal()));
            return entry;
        }).collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug(result);
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
        result.stream().filter(entry -> entry.getDate().isBefore(limitFrom) || entry.getDate().isAfter(limitTo)).findAny()
            .ifPresent(entry -> {
                throw new NAVPSDownloadValidationException("Found out of range entry - " + entry);
            });
    }

}
