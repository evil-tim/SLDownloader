package com.sldlt.scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.service.NAVPSDownloader;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSService;

@Component
public class DownloaderScheduledTask {

    private static Logger LOG = Logger.getLogger(DownloaderScheduledTask.class);

    @Autowired
    private NAVPSDownloader navpsDownloader;

    @Autowired
    private FundService fundService;

    @Autowired
    private NAVPSService navpsService;

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void run() {
        LOG.debug("Running downloader for " + LocalDateTime.now().toString());
        List<NAVPSEntryDto> allNavpsList = navpsDownloader.findAvailableFunds().stream()
                .map(fund -> fundService.saveFund(fund).getCode())
                .map(fundCode -> navpsDownloader.fetchNAVPSFromPage(fundCode)).flatMap(List::stream)
                .collect(Collectors.toList());
        navpsService.saveNAVPS(allNavpsList);
    }

}
