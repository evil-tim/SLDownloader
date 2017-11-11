package com.sldlt.scheduled;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@Component
public class TaskManager {

    private static final Logger LOG = Logger.getLogger(TaskManager.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private FundService fundService;

    @Value("${navps.mindate}")
    private String minDateStr;

    @Autowired
    private NAVPSDownloaderService navpsDownloader;

    //@Scheduled(fixedRate = 7200000)
    public void run() {
        LOG.info("regenerating task list");
        getFunds();
        setupPastTasks();
    }

    private void getFunds() {
        navpsDownloader.findAvailableFunds().stream().forEach(fund -> fundService.saveFund(fund));
    }

    private void setupPastTasks() {
        final List<FundDto> fundList = fundService.listAllFunds();

        final LocalDate dateFromSingle = LocalDate.now().with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate dateToSingle = LocalDate.now().minusDays(1);

        while (!dateToSingle.isBefore(dateFromSingle)) {
            final DayOfWeek singleTaskDay = dateToSingle.getDayOfWeek();
            if (!singleTaskDay.equals(DayOfWeek.SATURDAY) && !singleTaskDay.equals(DayOfWeek.SUNDAY)) {
                final LocalDate internalDateTo = dateToSingle;
                fundList.forEach(fund -> {
                    taskService.createTask(fund.getCode(), internalDateTo, internalDateTo);
                });
            }
            dateToSingle = dateToSingle.minusDays(1);
        }

        final LocalDate minDate = LocalDate.parse(minDateStr);
        LocalDate dateFrom = LocalDate.now().with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate dateTo = LocalDate.now().with(WeekFields.ISO.dayOfWeek(), 7);

        while (dateFrom.isAfter(minDate)) {
            final LocalDate internalDateFrom = dateFrom = dateFrom.minusDays(7);
            final LocalDate internalDateTo = dateTo = dateTo.minusDays(7);
            fundList.forEach(fund -> {
                taskService.createTask(fund.getCode(), internalDateFrom, internalDateTo);
            });
        }

    }
}
