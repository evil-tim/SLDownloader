package com.sldlt.scheduled;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@Component
public class NavpsTaskSetupDispatcherJob extends BaseDispatcherJob {

    private static final Logger LOG = LogManager.getLogger(NavpsTaskSetupDispatcherJob.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private FundService fundService;

    @Value("${navps.mindate}")
    private String minDateStr;

    @Value("${task.updater.zone:GMT+8}")
    private String timeZone;

    @Autowired
    private NAVPSDownloaderService navpsDownloader;

    @Scheduled(cron = "${task.updater.cron:0 0 4 * * *}", zone = "${task.updater.zone:GMT+8}")
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running NAVPS task setup dispatcher");
        }

        dispatchJob("NAVPS task setup", this::runTask);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Completed NAVPS task setup dispatcher");
        }
    }

    private void runTask() {
        getFunds();
        setupPastTasks();
    }

    private void getFunds() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Updating funds");
        }
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        navpsDownloader.findAvailableFunds().stream().forEach(fund -> fundService.saveFund(fund));

        stopwatch.stop();

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed updating funds in " + stopwatch.getTotalTimeMillis() + "ms");
        }
    }

    private void setupPastTasks() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Setting up tasks");
        }

        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        final LocalDate currentDate = LocalDate.now(ZoneId.of(timeZone));
        final List<FundDto> fundList = fundService.listAllFunds();

        final LocalDate dateFromSingle = currentDate.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate dateToSingle = currentDate.minusDays(1);

        while (!dateToSingle.isBefore(dateFromSingle)) {
            final DayOfWeek singleTaskDay = dateToSingle.getDayOfWeek();
            if (!singleTaskDay.equals(DayOfWeek.SATURDAY) && !singleTaskDay.equals(DayOfWeek.SUNDAY)) {
                final LocalDate internalDateTo = dateToSingle;
                fundList.forEach(fund -> taskService.createTask(fund.getCode(), internalDateTo, internalDateTo));
            }
            dateToSingle = dateToSingle.minusDays(1);
        }

        final LocalDate minDate = LocalDate.parse(minDateStr);
        LocalDate dateFrom = currentDate.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate dateTo = currentDate.with(WeekFields.ISO.dayOfWeek(), 7);

        while (dateFrom.isAfter(minDate)) {
            final LocalDate internalDateFrom = dateFrom = dateFrom.minusDays(7);
            final LocalDate internalDateTo = dateTo = dateTo.minusDays(7);
            fundList.forEach(fund -> taskService.createTask(fund.getCode(), internalDateFrom, internalDateTo));
        }

        stopwatch.stop();

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed setting up tasks in " + stopwatch.getTotalTimeMillis() + "ms");
        }
    }
}
