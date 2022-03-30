package com.sldlt.navps.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSExporterService;
import com.sldlt.navps.service.NAVPSService;

@Service
public class NAVPSExporterServiceImpl implements NAVPSExporterService {

    private static final Logger LOG = LogManager.getLogger(NAVPSExporterServiceImpl.class);

    @Autowired
    private FundService fundService;

    @Autowired
    private NAVPSService navpsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String buildNavpsCsvContent() {
        return fundService.listAllFunds().stream().map(FundDto::getCode).map(navpsService::listAllNAVPS)
            .filter(navpsList -> navpsList != null && !navpsList.isEmpty()).map(this::convertNavpsListToCsv).collect(Collectors.joining());
    }

    private String convertNavpsListToCsv(List<NAVPSEntryDto> navpsList) {
        return navpsList.stream().filter(Objects::nonNull).map(this::convertNavpsToCsv).collect(Collectors.joining());
    }

    private String convertNavpsToCsv(NAVPSEntryDto navpsEntry) {
        StringBuilder navpsCsv = new StringBuilder();
        return navpsCsv.append(navpsEntry.getFund()).append(',').append(navpsEntry.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .append(',').append(navpsEntry.getValue()).append('\n').toString();
    }

    @Override
    public String buildNavpsJsonContent() {
        List<List<NAVPSEntryDto>> allFundsLists = fundService.listAllFunds().stream().map(FundDto::getCode).filter(StringUtils::hasText)
            .map(navpsService::listAllNAVPS).filter(navpsList -> navpsList != null && !navpsList.isEmpty()).toList();

        return IntStream.range(0, allFundsLists.size() - 1).mapToObj(i -> Pair.of(i, allFundsLists.get(i))).map(this::convertNavpsListToObj)
            .map(this::convertObjToJsonString).filter(StringUtils::hasText).collect(Collectors.joining("\n"));
    }

    private NAVPSListJsonDto convertNavpsListToObj(Pair<Integer, List<NAVPSEntryDto>> navpsListData) {
        List<NAVPSEntryDto> navpsList = navpsListData.getSecond();
        Collections.reverse(navpsList);
        NAVPSEntryDto navpsEntry = navpsList.get(0);

        return new NAVPSListJsonDto(navpsEntry.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00:00",
            makeNavpsValuesList(navpsList), Collections.singletonList(navpsListData.getFirst()));
    }

    private List<Object> makeNavpsValuesList(List<NAVPSEntryDto> navpsList) {
        List<Object> navpsValuesList = new LinkedList<>();

        int index = 0;
        NAVPSEntryDto navpsEntry = navpsList.get(index);
        LocalDate navpsDate = navpsEntry.getDate();
        LocalDate currentDate = LocalDate.now();

        do {
            if (navpsEntry != null && navpsEntry.getDate().equals(navpsDate)) {
                navpsValuesList.add(navpsEntry.getValue());
                index++;
                if (index < navpsList.size()) {
                    navpsEntry = navpsList.get(index);
                } else {
                    navpsEntry = null;
                }
            } else {
                navpsValuesList.add("NaN");
            }

            navpsDate = navpsDate.plusDays(1);
        } while (!navpsDate.isAfter(currentDate));

        return navpsValuesList;
    }

    private String convertObjToJsonString(NAVPSListJsonDto navpsListJsonDto) {
        try {
            return objectMapper.writeValueAsString(navpsListJsonDto);
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private class NAVPSListJsonDto {

        private String start;

        private List<Object> target;

        private List<Integer> cat;

        public NAVPSListJsonDto(String start, List<Object> target, List<Integer> cat) {
            this.start = start;
            this.target = target;
            this.cat = cat;
        }

        public String getStart() {
            return start;
        }

        public List<Object> getTarget() {
            return target;
        }

        public List<Integer> getCat() {
            return cat;
        }

    }

}
