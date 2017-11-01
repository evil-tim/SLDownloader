package com.sldlt.downloader.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sldlt.downloader.exception.NAVPSDownloadValidationException;
import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

@Service
public class NAVPSDownloaderServiceImpl implements NAVPSDownloaderService {

    private static final Logger LOG = Logger.getLogger(NAVPSDownloaderServiceImpl.class);

    private static final String TO_YEAR_FIELD_NAME = "toYear";

    private static final String TO_DAY_FIELD_NAME = "toDay";

    private static final String TO_MONTH_FIELD_NAME = "toMonth";

    private static final String FROM_DAY_FIELD_NAME = "fromDay";

    private static final String FROM_YEAR_FIELD_NAME = "fromYear";

    private static final String FROM_MONTH_FIELD_NAME = "fromMonth";

    private static final String FUND_NAME_FIELD_NAME = "fundCD";

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Value("${navps.fundslist.url}")
    private String fundsUrl;

    @Value("${navps.navpsvalue.url}")
    private String navpsUrl;

    @Override
    public List<FundDto> findAvailableFunds() {
        List<FundDto> result = Collections.emptyList();
        try {
            result = Jsoup.connect(fundsUrl).timeout(60000).get().getElementsByTag("select").stream()
                .filter(element -> element.attr("name").equals(FUND_NAME_FIELD_NAME)).findFirst()
                .map(element -> element.getElementsByTag("option")).orElse(new Elements()).stream().map(element -> {
                    final FundDto fund = new FundDto();
                    fund.setCode(element.attr("value"));
                    fund.setName(element.text().trim());
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
        LocalDate currentDate = LocalDate.now();
        return fetchNAVPSFromPage(fund, currentDate, currentDate);
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(final FundDto fund, final LocalDate limitFrom, final LocalDate limitTo)
                    throws IOException {

        final Document document = Jsoup.connect(navpsUrl).data(FUND_NAME_FIELD_NAME, fund.getCode())
            .data(FROM_MONTH_FIELD_NAME, String.valueOf(limitFrom.getMonthValue()))
            .data(FROM_DAY_FIELD_NAME, String.valueOf(limitFrom.getDayOfMonth()))
            .data(FROM_YEAR_FIELD_NAME, String.valueOf(limitFrom.getYear()))
            .data(TO_MONTH_FIELD_NAME, String.valueOf(limitTo.getMonthValue()))
            .data(TO_DAY_FIELD_NAME, String.valueOf(limitTo.getDayOfMonth()))
            .data(TO_YEAR_FIELD_NAME, String.valueOf(limitTo.getYear())).timeout(60000).post();

        validateFundNameMatches(document, fund);

        final List<NAVPSEntryDto> result = document.getElementsByTag("table").get(2).getElementsByTag("tr").stream().skip(2)
            .map(row -> {
                Elements cells = row.getElementsByTag("td");
                NAVPSEntryDto entry = new NAVPSEntryDto();
                entry.setFund(fund.getCode());
                entry.setDate(LocalDate.parse(cells.get(1).text().trim(), format));
                entry.setValue(new BigDecimal(cells.get(3).text().trim()));
                return entry;
            }).collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug(result);
        }

        validateResultsNotEmpty(result);

        validateResultsInRange(result, limitFrom, limitTo);

        return result;
    }

    private void validateFundNameMatches(final Document document, final FundDto fund) {
        String documentFund = null;
        String documentFundStr = document.getElementsByTag("table").get(1).getElementsByTag("b").html();
        if (StringUtils.hasText(documentFundStr)) {
            final String[] documentFundStrParts = documentFundStr.split("<br />");
            if (documentFundStrParts.length > 1) {
                documentFund = documentFundStrParts[0].trim();
            }
        }
        if (!StringUtils.hasText(documentFund) || !fund.getName().toUpperCase().contains(documentFund)) {
            throw new NAVPSDownloadValidationException(
                "Found mismatched entry - [" + documentFund + "] should be [" + fund.getName().toUpperCase() + "]");
        }
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
