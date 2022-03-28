package com.sldlt.scheduled;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;

@Component
public class TaskExecutorJob {

    private static final Logger LOG = LogManager.getLogger(TaskExecutorJob.class);

    @Value("${task.maxRunning}")
    private Integer maxRunningTasks;

    @Autowired
    private RunningTaskHolder runningTaskHolder;

    @Autowired
    private TaskService taskService;

    @Autowired
    private NAVPSTaskExecutorService navpsTaskExecutorService;

    @Scheduled(initialDelay = 120000, fixedRateString = "${task.executor.rate:30000}")
    public void run() {
        LOG.info("Running executor");
        if (runningTaskHolder.size() >= maxRunningTasks) {
            return;
        }

        final List<TaskDto> tasks = taskService.getExecutableTasks(maxRunningTasks);

        tasks.parallelStream().filter(task -> runningTaskHolder.add(task.getId())).forEach(task -> {
            if (LOG.isInfoEnabled()) {
                LOG.info("Executing " + task);
            }
            navpsTaskExecutorService.executeTask(task);
            runningTaskHolder.remove(task.getId());
        });
    }
}
