package com.sldlt.scheduled;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSService;

@Component
@Profile("aws")
public class DataExporter {

    private static final Logger LOG = Logger.getLogger(DataExporter.class);

    @Value("${dataexporter.csv.s3.bucket.name}")
    private String bucketName;

    @Value("${dataexporter.csv.s3.bucket.path}")
    private String path;

    @Value("${dataexporter.csv.filename}")
    private String filename;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private FundService fundService;

    @Autowired
    private NAVPSService navpsService;

    @Scheduled(cron = "${dataexporter.cron:0 0 5 * * *}", zone = "${dataexporter.zone:GMT+8}")
    public void run() {
        LOG.info("Uploading NAVPS to " + bucketName + "/" + path + filename);
        amazonS3.putObject(bucketName, path + filename, buildNavpsCsvContent());
    }

    private String buildNavpsCsvContent() {
        return fundService.listAllFunds().stream().map(FundDto::getCode).map(navpsService::listAllNAVPS)
            .map(this::convertNavpsListToCsv).collect(Collectors.joining());
    }

    private String convertNavpsListToCsv(List<NAVPSEntryDto> navpsList) {
        if (navpsList == null || navpsList.isEmpty()) {
            return "";
        }
        return navpsList.stream().map(this::convertNavpsToCsv).collect(Collectors.joining());
    }

    private String convertNavpsToCsv(NAVPSEntryDto navpsEntry) {
        if (navpsEntry == null) {
            return "";
        }
        StringBuilder navpsCsv = new StringBuilder();
        return navpsCsv.append(navpsEntry.getFund()).append(',').append(navpsEntry.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .append(',').append(navpsEntry.getValue()).append('\n').toString();
    }
}
