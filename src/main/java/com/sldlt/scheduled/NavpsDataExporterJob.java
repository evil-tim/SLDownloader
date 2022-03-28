package com.sldlt.scheduled;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.sldlt.navps.service.NAVPSExporterService;

@Component
@Profile("aws")
public class NavpsDataExporterJob {

    private static final Logger LOG = LogManager.getLogger(NavpsDataExporterJob.class);

    @Value("${dataexporter.s3.bucket.name}")
    private String bucketName;

    @Value("${dataexporter.s3.bucket.path}")
    private String path;

    @Value("${dataexporter.csv.filename}")
    private String csvFilename;

    @Value("${dataexporter.json.filename}")
    private String jsonFilename;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private NAVPSExporterService navpsExporterService;

    @Scheduled(cron = "${dataexporter.cron:0 0 5 * * *}", zone = "${dataexporter.zone:GMT+8}")
    public void run() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Uploading NAVPS to " + bucketName + "/" + path + csvFilename);
        }
        amazonS3.putObject(bucketName, path + csvFilename, navpsExporterService.buildNavpsCsvContent());

        if (LOG.isInfoEnabled()) {
            LOG.info("Uploading NAVPS to " + bucketName + "/" + path + jsonFilename);
        }
        amazonS3.putObject(bucketName, path + jsonFilename, navpsExporterService.buildNavpsJsonContent());
    }

}
