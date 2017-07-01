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

import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;

@Service
public class NAVPSDownloaderServiceImpl implements NAVPSDownloaderService {

    private static Logger LOG = Logger.getLogger(NAVPSDownloaderServiceImpl.class);

    private static final String TO_YEAR_FIELD_NAME = "toYear";

    private static final String TO_DAY_FIELD_NAME = "toDay";

    private static final String TO_MONTH_FIELD_NAME = "toMonth";

    private static final String FROM_DAY_FIELD_NAME = "fromDay";

    private static final String FROM_YEAR_FIELD_NAME = "fromYear";

    private static final String FROM_MONTH_FIELD_NAME = "fromMonth";

    private static final String FUND_NAME_FIELD_NAME = "fundCD";

    private DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Value("${navps.fundslist.url}")
    private String fundsUrl;

    @Value("${navps.navpsvalue.url}")
    private String navpsUrl;

    @Override
    public List<FundDto> findAvailableFunds() {
        try {
            List<FundDto> result = Jsoup.connect(fundsUrl).timeout(60000).get().getElementsByTag("select").stream()
                    .filter(element -> element.attr("name").equals(FUND_NAME_FIELD_NAME)).findFirst()
                    .map(element -> element.getElementsByTag("option")).orElse(new Elements()).stream().map(element -> {
                        FundDto fund = new FundDto();
                        fund.setCode(element.attr("value"));
                        fund.setName(element.text().trim());
                        return fund;
                    }).collect(Collectors.toList());
            LOG.debug(result);
            return result;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund, LocalDate limitFrom, LocalDate limitTo)
            throws IOException {
        try {
            Document document = Jsoup.connect(navpsUrl).data(FUND_NAME_FIELD_NAME, fund.getCode())
                    .data(FROM_MONTH_FIELD_NAME, "" + limitFrom.getMonthValue())
                    .data(FROM_DAY_FIELD_NAME, "" + limitFrom.getDayOfMonth())
                    .data(FROM_YEAR_FIELD_NAME, "" + limitFrom.getYear())
                    .data(TO_MONTH_FIELD_NAME, "" + limitTo.getMonthValue())
                    .data(TO_DAY_FIELD_NAME, "" + limitTo.getDayOfMonth())
                    .data(TO_YEAR_FIELD_NAME, "" + limitTo.getYear()).timeout(60000).post();

            validateFundNameMatches(document, fund);

            List<NAVPSEntryDto> result = document.getElementsByTag("table").get(2).getElementsByTag("tr").stream()
                    .skip(2).map(row -> {
                        Elements cells = row.getElementsByTag("td");
                        NAVPSEntryDto entry = new NAVPSEntryDto();
                        entry.setFund(fund.getCode());
                        entry.setDate(LocalDate.parse(cells.get(1).text().trim(), format));
                        entry.setValue(new BigDecimal(cells.get(3).text().trim()));
                        return entry;
                    }).collect(Collectors.toList());

            LOG.debug(result);

            validateResultsNotEmpty(result);

            validateResultsInRange(result, limitFrom, limitTo);

            return result;
        } catch (IOException e) {
            throw e;
        }
    }

    private void validateFundNameMatches(Document document, FundDto fund) {
        String documentFund = null;
        String documentFundStr = document.getElementsByTag("table").get(1).getElementsByTag("b").html();
        if (StringUtils.hasText(documentFundStr)) {
            String[] documentFundStrParts = documentFundStr.split("<br />");
            if (documentFundStrParts.length > 1) {
                documentFund = documentFundStrParts[0].trim();
            }
        }
        if (!fund.getName().toUpperCase().contains(documentFund)) {
            String message = "Found mismatched entry - [" + documentFund + "] should be ["
                    + fund.getName().toUpperCase() + "]";
            throw new RuntimeException(message);
        }
    }

    private void validateResultsNotEmpty(List<NAVPSEntryDto> result) {
        if (result == null || result.isEmpty()) {
            String message = "No entries found";
            throw new RuntimeException(message);
        }
    }

    private void validateResultsInRange(List<NAVPSEntryDto> result, LocalDate limitFrom, LocalDate limitTo) {
        result.stream().filter(entry -> entry.getDate().isBefore(limitFrom) || entry.getDate().isAfter(limitTo))
                .findAny().ifPresent(entry -> {
                    String message = "Found out of range entry - " + entry;
                    throw new RuntimeException(message);
                });
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(FundDto fund) throws IOException {
        LocalDate currentDate = LocalDate.now();
        return fetchNAVPSFromPage(fund, currentDate, currentDate);
    }

}
