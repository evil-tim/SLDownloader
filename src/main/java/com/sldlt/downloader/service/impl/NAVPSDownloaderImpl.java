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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sldlt.downloader.service.NAVPSDownloader;
import com.sldlt.navps.dto.NAVPSEntryDto;

@Service
public class NAVPSDownloaderImpl implements NAVPSDownloader {

    private static Logger LOG = Logger.getLogger(NAVPSDownloaderImpl.class);

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
    public List<String> findAvailableFunds() {
        try {
            List<String> result = Jsoup.connect(fundsUrl).timeout(60000).get().getElementsByTag("select").stream()
                    .filter(element -> element.attr("name").equals(FUND_NAME_FIELD_NAME)).findFirst()
                    .map(element -> element.getElementsByTag("option")).orElse(new Elements()).stream()
                    .map(element -> element.attr("value")).collect(Collectors.toList());
            LOG.debug(result);
            return result;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(String fund, LocalDate limitFrom, LocalDate limitTo) {
        try {
            List<NAVPSEntryDto> result = Jsoup.connect(navpsUrl).data(FUND_NAME_FIELD_NAME, fund)
                    .data(FROM_MONTH_FIELD_NAME, "" + limitFrom.getMonthValue())
                    .data(FROM_DAY_FIELD_NAME, "" + limitFrom.getDayOfMonth())
                    .data(FROM_YEAR_FIELD_NAME, "" + limitFrom.getYear())
                    .data(TO_MONTH_FIELD_NAME, "" + limitTo.getMonthValue())
                    .data(TO_DAY_FIELD_NAME, "" + limitTo.getDayOfMonth())
                    .data(TO_YEAR_FIELD_NAME, "" + limitTo.getYear()).timeout(60000).post().getElementsByTag("table")
                    .get(2).getElementsByTag("tr").stream().skip(2).map(row -> {
                        Elements cells = row.getElementsByTag("td");
                        NAVPSEntryDto entry = new NAVPSEntryDto();
                        entry.setFund(fund);
                        entry.setDate(LocalDate.parse(cells.get(1).text().trim(), format));
                        entry.setValue(new BigDecimal(cells.get(3).text().trim()));
                        return entry;
                    }).collect(Collectors.toList());
            LOG.debug(result);
            return result;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<NAVPSEntryDto> fetchNAVPSFromPage(String fund) {
        LocalDate currentDate = LocalDate.now();
        return fetchNAVPSFromPage(fund, currentDate, currentDate);
    }

}
