package com.sldlt.scheduled;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;

@Component
public class TaskExecutor {

    private static Logger LOG = Logger.getLogger(TaskExecutor.class);

    @Value("${task.maxRunning}")
    private Integer maxRunningTasks;

    private Set<Long> runningTaskIdSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

    @Autowired
    private TaskService taskService;

    @Autowired
    private NAVPSTaskExecutorService navpsTaskExecutorService;

    @Scheduled(initialDelay = 120000, fixedRate = 1000)
    public void run() {
        LOG.info("Running executor");
        if (runningTaskIdSet.size() >= maxRunningTasks) {
            return;
        }

        List<TaskDto> tasks = taskService.getExecutableTasks(maxRunningTasks);

        tasks.parallelStream().filter(task -> runningTaskIdSet.add(task.getId())).forEach(task -> {
            LOG.info("Executing " + task);
            navpsTaskExecutorService.executeTask(task);
            runningTaskIdSet.remove(task.getId());
        });
    }
}
