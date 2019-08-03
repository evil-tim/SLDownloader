package com.sldlt.downloader.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    private static final String TO_DATE_FIELD_NAME = "toDate";

    private static final String FROM_DATE_FIELD_NAME = "fromDate";

    private static final String FUND_NAME_FIELD_NAME = "fundCD";

    private final DateTimeFormatter responseDateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final DateTimeFormatter paramDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

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

        final Document document = Jsoup.connect(navpsUrl).data(FUND_NAME_FIELD_NAME, fund.getCode())
            .data(FROM_DATE_FIELD_NAME, String.valueOf(limitFrom.format(paramDateFormat)))
            .data(TO_DATE_FIELD_NAME, String.valueOf(limitTo.format(paramDateFormat))).timeout(60000).get();

        validateFundNameMatches(document, fund);

        final List<NAVPSEntryDto> result = document.getElementsByTag("table").get(0).getElementsByTag("tbody").get(0)
            .getElementsByTag("tr").stream().map(row -> {
                Elements cells = row.getElementsByTag("td");
                NAVPSEntryDto entry = new NAVPSEntryDto();
                entry.setFund(fund.getCode());
                entry.setDate(LocalDate.parse(cells.get(0).text().trim(), responseDateFormat));
                entry.setValue(new BigDecimal(cells.get(1).text().split(" ")[1].trim()));
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
        final Optional<String> fundName = Optional.ofNullable(document).map(doc -> doc.getElementsByTag("span"))
            .map(elements -> elements.get(0)).map(Element::text).map(String::trim).map(String::toUpperCase);

        if (!fundName.filter(StringUtils::hasText)
            .filter(fundNameStr -> matchFundNames(fund.getCode(), fund.getName().toUpperCase(), fundNameStr)).isPresent()) {
            throw new NAVPSDownloadValidationException(
                "Found mismatched entry - [" + fundName.orElse("") + "] should be [" + fund.getName().toUpperCase() + "]");
        }
    }

    private boolean matchFundNames(final String fundCode, final String expectedFundName, final String actualFundName) {
        if ("CF0006".equals(fundCode)) {
            return expectedFundName.contains(actualFundName) || "DOLLAR ABUNDANCE FUND".equals(actualFundName);
        } else {
            return expectedFundName.contains(actualFundName);
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
