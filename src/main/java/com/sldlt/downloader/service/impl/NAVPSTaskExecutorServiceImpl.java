package com.sldlt.downloader.service.impl;

import static com.sldlt.downloader.TaskStatus.FAILED;
import static com.sldlt.downloader.TaskStatus.SUCCESS;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSDownloaderService;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSService;

@Service
@Transactional
public class NAVPSTaskExecutorServiceImpl implements NAVPSTaskExecutorService {

    private static Logger LOG = Logger.getLogger(NAVPSTaskExecutorServiceImpl.class);

    @Autowired
    private NAVPSDownloaderService navpsDownloader;

    @Autowired
    private NAVPSService navpsService;

    @Autowired
    private TaskService taskService;

    @Override
    public TaskStatus executeTask(TaskDto task) {
        try {
            List<NAVPSEntryDto> navpsList = navpsDownloader.fetchNAVPSFromPage(task.getFund(), task.getDateFrom(),
                    task.getDateTo());
            navpsService.saveNAVPS(navpsList);
            taskService.updateTaskStatus(task.getId(), SUCCESS);
            return SUCCESS;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            taskService.updateTaskStatus(task.getId(), FAILED);
            return FAILED;
        }
    }

}
