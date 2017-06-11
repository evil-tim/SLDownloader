package com.sldlt.scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;

@Component
public class TaskExecutor {

    private static Logger LOG = Logger.getLogger(TaskExecutor.class);

    private Set<Long> runningTaskIdSet = new HashSet<Long>();

    @Autowired
    private TaskService taskService;

    @Autowired
    private NAVPSTaskExecutorService navpsTaskExecutorService;

    @Scheduled(initialDelay = 120000, fixedRate = 10000)
    public void run() {
        LOG.info("Running executor");
        List<TaskDto> tasks = taskService.getExecutableTasks(5);

        for (TaskDto task : tasks) {
            if (attempToRegisterTask(task)) {
                LOG.info("Executing " + task);
                navpsTaskExecutorService.executeTask(task);
                runningTaskIdSet.remove(task.getId());
                break;
            }
        }
    }

    private synchronized boolean attempToRegisterTask(TaskDto task) {
        return !runningTaskIdSet.contains(task.getId()) && runningTaskIdSet.add(task.getId());
    }
}
