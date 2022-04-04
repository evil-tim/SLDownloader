package com.sldlt.downloader.service.impl;

import static com.sldlt.downloader.TaskStatus.FAILED;
import static com.sldlt.downloader.TaskStatus.SUCCESS;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSService;

@Service
public class NAVPSTaskExecutorServiceImpl implements NAVPSTaskExecutorService {

    private static final Logger LOG = LogManager.getLogger(NAVPSTaskExecutorServiceImpl.class);

    @Autowired
    private NAVPSDownloaderService navpsDownloader;

    @Autowired
    private NAVPSService navpsService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FundService fundService;

    @Override
    @Transactional
    public TaskStatus executeTask(final TaskDto task) {
        TaskStatus finalStatus = null;
        try {
            final List<NAVPSEntryDto> navpsList = navpsDownloader.fetchNAVPSFromPage(fundService.getFundByCode(task.getFund()),
                task.getDateFrom(), task.getDateTo());
            navpsService.saveNAVPS(navpsList);
            taskService.updateTaskSucceeded(task.getId());
            finalStatus = SUCCESS;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            taskService.updateTaskFailed(task.getId());
            finalStatus = FAILED;
        }
        return finalStatus;
    }

}
