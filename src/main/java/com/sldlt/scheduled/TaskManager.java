package com.sldlt.scheduled;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@Component
public class TaskManager {

    private static Logger LOG = Logger.getLogger(TaskManager.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private FundService fundService;

    @Value("${navps.mindate}")
    private String minDateStr;

    @Autowired
    private NAVPSDownloaderService navpsDownloader;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void run() {
        getFunds();
        setupPastTasks();
        setupCurrentTask();
    }

    private void getFunds() {
        navpsDownloader.findAvailableFunds().stream().forEach(fund -> fundService.saveFund(fund));
    }

    private void setupCurrentTask() {
        fundService.listAllFunds().forEach(fund -> {
            taskService.createTask(fund.getCode(), LocalDate.now(), LocalDate.now());
        });
    }

    private void setupPastTasks() {
        LocalDate minDate = LocalDate.parse(minDateStr);
        LocalDate dateFrom = LocalDate.now().with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate dateTo = LocalDate.now().with(WeekFields.ISO.dayOfWeek(), 7);
        List<FundDto> taskList = fundService.listAllFunds();

        while (dateFrom.isAfter(minDate)) {
            final LocalDate internalDateFrom = dateFrom = dateFrom.minusDays(7);
            final LocalDate internalDateTo = dateTo = dateTo.minusDays(7);
            taskList.forEach(fund -> {
                taskService.createTask(fund.getCode(), internalDateFrom, internalDateTo);
            });
        }
    }
}
